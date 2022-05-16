package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static pt.up.fe.comp.optimization.OllirUtils.*;
import static pt.up.fe.comp.semantic.tables.JmmSymbolTable.getClosestMethod;
import static pt.up.fe.comp.semantic.tables.JmmSymbolTable.getMethodName;

public class OllirEmitter extends AJmmVisitor<SubstituteVariable, Boolean> {
    private final StringBuilder ollirCode;
    private final JmmSymbolTable symbolTable;
    private int temporaryVariableCounter = -1;
    private int ifCounter = -1;
    private int loopCounter = -1;
    private int indentationLevel = 0;
    private final int numberOfSpaces;

    public OllirEmitter(StringBuilder ollirCode, JmmSymbolTable symbolTable, int numberOfSpaces) {
        this.ollirCode = ollirCode;
        this.symbolTable = symbolTable;
        this.numberOfSpaces = numberOfSpaces;
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("VarDeclaration", this::visitFieldDeclaration);
        addVisit("RegularMethod", this::visitMethod);
        addVisit("MainMethod", this::visitMethod);
        addVisit("MethodBody", this::visitAllChildren);
        addVisit("Return", this::visitReturn);
        addVisit("If", this::visitIf);
        addVisit("While", this::visitWhile);
        addVisit("BinOp", this::visitBinOp);
        addVisit("UnaryOp", this::visitUnaryOp);
        addVisit("CompoundExpression", this::visitCompoundExpression);
        addVisit("Indexation", this::visitIndexation);
        addVisit("AttributeGet", this::visitAttributeGet);
        addVisit("MethodCall", this::visitMethodCall);
        addVisit("ArrayElement", this::visitArrayElement);
        addVisit("NewArray", this::visitNewArray);
        addVisit("NewObject", this::visitNewObject);
        addVisit("Identifier", this::visitIdentifier);
        addVisit("IntLiteral", this::visitIntLiteral);
        addVisit("BooleanLiteral", this::visitBooleanLiteral);
        addVisit("ThisLiteral", this::visitThisLiteral);
        setDefaultVisit((node, dummy) -> null);
    }

    private void startNewLine() {
        ollirCode.append("\n").append(IntStream.range(0, numberOfSpaces * indentationLevel).mapToObj(i -> " ").collect(Collectors.joining("")));
    }

    private String createTemporaryAssign(String variableName, String ollirType, String value){
        return variableName + "." + ollirType + " :=." + ollirType + " " + value + ";";
    }

    private SubstituteVariable createTemporaryVariable(JmmNode closestNode) {
        temporaryVariableCounter += 1;
        String name = "t" + temporaryVariableCounter;
        while (symbolTable.getClosestSymbol(closestNode, name).isPresent()){
            name = "_" + name;
        }
        return new SubstituteVariable(name);
    }

    private void fillImports() {
        for (var imp : symbolTable.getImports()) {
            ollirCode.append("import ").append(imp).append(";");
            startNewLine();
        }
    }

    private Boolean visitStart(JmmNode node, SubstituteVariable dummy) {
        fillImports();
        visitAllChildren(node, dummy);
        return null;
    }

    private Boolean visitClassDeclaration(JmmNode node, SubstituteVariable dummy) {
        String className = node.get("name");
        ollirCode.append(className).append(" ");
        if (symbolTable.getSuper() != null) {
            ollirCode.append("extends ").append(symbolTable.getSuper()).append(" ");
        }
        ollirCode.append("{");
        indentationLevel++;
        for (var child : node.getChildren().stream().filter((n) -> n.getKind().equals("VarDeclaration")).collect(Collectors.toList())) {
            startNewLine();
            ollirCode.append(".field private ");
            visit(child);
        }
        addVisit("VarDeclaration", (n, d) -> null);
        startNewLine();

        ollirCode.append(".construct ").append(className).append("().V {");
        indentationLevel++;
        startNewLine();
        ollirCode.append(invoke("invokespecial", "this", "<init>", new ArrayList<>(), "V")).append(";");
        indentationLevel--;
        startNewLine();
        ollirCode.append("}");

        for (var child : node.getChildren().stream().filter((n) -> n.getKind().equals("RegularMethod")).collect(Collectors.toList())) {
            startNewLine();
            ollirCode.append(".method public ");
            visit(child);
        }
        for (var child : node.getChildren().stream().filter((n) -> n.getKind().equals("MainMethod")).collect(Collectors.toList())) {
            startNewLine();
            ollirCode.append(".method public static ");
            visit(child);
        }

        indentationLevel--;
        startNewLine();
        ollirCode.append("}");
        return null;
    }

    private Boolean visitFieldDeclaration(JmmNode node, SubstituteVariable dummy) {
        String variableName = node.getChildren().get(1).get("name");
        Type type = new Type(node.getChildren().get(0).get("name"), Boolean.parseBoolean(node.getChildren().get(0).get("isArray")));
        ollirCode.append(variableName).append(".").append(getOllirType(type));
        ollirCode.append(";");
        return null;
    }

    private Boolean visitMethod(JmmNode node, SubstituteVariable dummy) {
        String methodName = node.getKind().equals("RegularMethod") ? node.getChildren().get(1).get("name") : "main";
        var returnType = symbolTable.getReturnType(methodName);
        ollirCode.append(methodName).append("(");
        var parameters = symbolTable.getParameters(methodName);
        if (!parameters.isEmpty()) {
            for (var parameter : symbolTable.getParameters(methodName)) {
                ollirCode.append(parameter.getName()).append(".").append(getOllirType(parameter)).append(", ");
            }
            ollirCode.delete(ollirCode.lastIndexOf(","), ollirCode.length());
        }
        ollirCode.append(").").append(getOllirType(returnType)).append(" {");
        indentationLevel++;
        for (var child : node.getChildren()) {
            if (child.getKind().equals("MethodBody") || child.getKind().equals("Return")){
                visit(child);
            }
        }
        indentationLevel--;
        startNewLine();
        ollirCode.append("}");
        return null;
    }

    private Boolean visitIf(JmmNode node, SubstituteVariable dummy) {
        ifCounter++;
        SubstituteVariable conditionHolder = createTemporaryVariable(node);
        visit(node.getJmmChild(0), conditionHolder);
        startNewLine();
        ollirCode.append("if (!.bool ").append(conditionHolder.getSubstitute())
                .append(") goto Else").append(ifCounter).append(";");
        startNewLine();
        ollirCode.append("Then").append(ifCounter).append(":");
        indentationLevel++;
        for (var thenChild : node.getJmmChild(1).getChildren()){
            visit(thenChild);
        }
        startNewLine();
        ollirCode.append("goto EndIf").append(ifCounter).append(";");
        indentationLevel--;
        startNewLine();
        ollirCode.append("Else").append(ifCounter).append(":");
        indentationLevel++;
        for (var elseChild : node.getJmmChild(2).getChildren()){
            visit(elseChild);
        }
        indentationLevel--;
        startNewLine();
        ollirCode.append("EndIf").append(ifCounter).append(":");
        return true;
    }

    private Boolean visitWhile(JmmNode node, SubstituteVariable dummy) {
        loopCounter++;
        startNewLine();
        ollirCode.append("Loop").append(loopCounter).append(":");
        indentationLevel++;
        SubstituteVariable conditionHolder = createTemporaryVariable(node);
        visit(node.getJmmChild(0), conditionHolder);
        startNewLine();
        ollirCode.append("if (!.bool ").append(conditionHolder.getSubstitute())
                .append(") goto End").append(loopCounter).append(";");
        for (var bodyChild : node.getJmmChild(1).getChildren()){
            visit(bodyChild);
        }
        startNewLine();
        ollirCode.append("goto Loop").append(loopCounter).append(";");
        indentationLevel--;
        startNewLine();
        ollirCode.append("End").append(loopCounter).append(":");
        return true;
    }

    private Boolean visitBinOp(JmmNode node, SubstituteVariable substituteVariable) {
        var t1 = createTemporaryVariable(node);
        var t2 = createTemporaryVariable(node);
        visit(node.getJmmChild(0), t1);
        t2.setVariableType(t1.getVariableType());
        visit(node.getJmmChild(1), t2);

        String operation = node.get("op");
        if (operation.equals("assign")) {
            String variableName = t1.getVariableName();
            boolean isArray = symbolTable.getClosestSymbol(node, variableName).get().getType().isArray();
            String ollirType = getOllirType(t1.getVariableType());
            if (!isArray && symbolTable.getFields().stream().anyMatch(s -> s.getName().equals(variableName))){
                startNewLine();
                ollirCode.append(OllirUtils.putField("this", t1.getSubstitute(), t2.getSubstitute()));
            } else {
                startNewLine();
                ollirCode.append(t1.getSubstitute()).append(" :=.").append(ollirType)
                        .append(" ").append(t2.getSubstitute()).append(";");
            }
            return true;
        } else {
            String operationCode = "OP";
            switch (operation) {
                case "or":
                    operationCode = "||";
                    break;
                case "and":
                    operationCode = "&&";
                    break;
                case "lt":
                    operationCode = "<";
                    break;
                case "add":
                    operationCode = "+";
                    break;
                case "sub":
                    operationCode = "-";
                    break;
                case "mul":
                    operationCode = "*";
                    break;
                case "div":
                    operationCode = "/";
                    break;
            }
            Type operationType = !operationCode.equals("<") ? t1.getVariableType() : new Type("boolean", false);
            String tempType = getOllirType(operationType);
            String code = t1.getSubstitute()  + " " + operationCode + "." + tempType + " " + t2.getSubstitute();
            String tempPrefix = substituteVariable.getVariableName() + "." + tempType + " :=." + tempType + " ";
            substituteVariable.setVariableType(operationType);
            startNewLine();
            ollirCode.append(tempPrefix).append(code).append(";");
            return true;
        }
    }

    private Boolean visitUnaryOp(JmmNode node, SubstituteVariable substituteVariable) {
        if (node.get("op").equals("not")) {
            SubstituteVariable childHolder = createTemporaryVariable(node);
            visit(node.getJmmChild(0), childHolder);
            startNewLine();
            ollirCode.append(createTemporaryAssign(substituteVariable.getVariableName(), getOllirType(childHolder.getVariableType()),
                    "!.bool " + childHolder.getSubstitute()));
            substituteVariable.setVariableType(childHolder.getVariableType());
        }
        return true;
    }

    private Boolean visitArrayElement(JmmNode node, SubstituteVariable substituteVariable) {
        String variableName = node.getJmmChild(0).get("name");
        Symbol symbol = symbolTable.getClosestSymbol(node.getJmmChild(0), variableName).get();

        // Visit indexation
        SubstituteVariable positionChild = createTemporaryVariable(node);
        visit(node.getJmmChild(1), positionChild);

        // Get element type
        substituteVariable.setVariableName(variableName);
        Type elementType = new Type(symbol.getType().getName(), false);

        // Hold indexation child in variable
        startNewLine();
        SubstituteVariable positionTemporaryVariable = createTemporaryVariable(node);
        ollirCode.append(createTemporaryAssign(positionTemporaryVariable.getVariableName(),
                getOllirType(elementType), positionChild.getSubstitute()));

        boolean isClassField = symbolTable.getFields().stream().anyMatch(s -> s.getName().equals(variableName));
        if (isClassField) {
            SubstituteVariable referenceHolder = createTemporaryVariable(node);
            startNewLine();
            ollirCode.append(createTemporaryAssign(referenceHolder.getVariableName(), getOllirType(elementType),
                    getField("this", variableName, getOllirType(elementType))));
            substituteVariable.setValue(referenceHolder.getVariableName() + "[" + positionTemporaryVariable.getSubstitute() + "]");
        } else {
            substituteVariable.setValue(variableName + "[" + positionTemporaryVariable.getSubstitute() + "]");
        }
        substituteVariable.setVariableType(elementType);
        return true;
    }

    private boolean visitIndexation(JmmNode node, SubstituteVariable accessedVariable) {
        Type elementType = new Type(accessedVariable.getVariableType().getName(), false);

        // Visit indexation
        SubstituteVariable positionChild = createTemporaryVariable(node);
        visit(node.getJmmChild(0), positionChild);

        // Hold indexation child in variable
        startNewLine();
        SubstituteVariable positionTemporaryVariable = createTemporaryVariable(node);
        ollirCode.append(createTemporaryAssign(positionTemporaryVariable.getVariableName(),
                getOllirType(positionChild.getVariableType()), positionChild.getSubstitute()));

        // Store indexed variable in temporary variable
        startNewLine();
        SubstituteVariable indexVariable = createTemporaryVariable(node);
        ollirCode.append(createTemporaryAssign(indexVariable.getVariableName(), getOllirType(elementType),
                accessedVariable.getValue() + "[" + positionTemporaryVariable.getSubstitute() + "]." + getOllirType(elementType)));
        accessedVariable.setValue(indexVariable.getVariableName());
        accessedVariable.setVariableType(elementType);

        return true;
    }

    private Boolean visitAttributeGet(JmmNode node, SubstituteVariable accessedVariable) {
        // Handle the length case
        if (node.getJmmChild(0).getOptional("name").isPresent()
                && node.getJmmChild(0).get("name").equals("length")) {
            SubstituteVariable lengthHolder = createTemporaryVariable(node);
            startNewLine();
            ollirCode.append(createTemporaryAssign(lengthHolder.getVariableName(), "i32",
                    arrayLength(accessedVariable.getValue(), "i32")));
            accessedVariable.setValue(lengthHolder.getVariableName());
            accessedVariable.setVariableType(new Type("int", false));
            return true;
        }

        // Visit attribute
        SubstituteVariable attributeChild = createTemporaryVariable(node);
        visit(node.getJmmChild(0), attributeChild);
        Type elementType = attributeChild.getVariableType();

        // Store attribute variable in temporary variable
        startNewLine();
        SubstituteVariable attributeVariable = createTemporaryVariable(node);
        ollirCode.append(createTemporaryAssign(attributeVariable.getVariableName(), getOllirType(attributeChild.getVariableType()),
                getField(accessedVariable.getValue(), attributeChild.getVariableName(), getOllirType(elementType))));
        accessedVariable.setValue(attributeVariable.getVariableName());
        accessedVariable.setVariableType(elementType);

        return true;
    }

    private Boolean visitMethodCall(JmmNode node, SubstituteVariable accessedVariable) {
        // Visit arguments
        var arguments = new ArrayList<SubstituteVariable>();
        for (var arg : node.getJmmChild(1).getChildren()){
            SubstituteVariable methodArgument = createTemporaryVariable(node);
            visit(arg, methodArgument);
            arguments.add(methodArgument);
        }

        // Write method call
        startNewLine();
        String methodName = node.getJmmChild(0).get("name");
        Type methodType = new Type("void", false);;
        if (symbolTable.getReturnType(methodName) != null){
            methodType = symbolTable.getReturnType(methodName);
        } else {
            if (node.getJmmParent().getChildren().stream().anyMatch(c -> c.getKind().equals("Indexation"))){
                methodType = new Type("int", true);
            } else if (node.getAncestor("BinOp").isPresent()
                    && node.getAncestor("BinOp").get().get("op").equals("assign")){
                if (accessedVariable != null && accessedVariable.getVariableType() != null) {
                    methodType = accessedVariable.getVariableType();
                }
            }
        }
        String ollirMethodType = getOllirType(methodType);
        SubstituteVariable methodCallHolder = createTemporaryVariable(node);
        boolean isVirtualCall = accessedVariable.getValue().equals("this")
                || symbolTable.getClosestSymbol(node, accessedVariable.getValue()).isPresent();
        ollirCode.append(createTemporaryAssign(methodCallHolder.getVariableName(), ollirMethodType,
                invoke(isVirtualCall ? "invokevirtual" : "invokestatic", accessedVariable.getValue(), node.getJmmChild(0).get("name"),
                arguments.stream().map(SubstituteVariable::getSubstitute).collect(Collectors.toList()), ollirMethodType)));
        accessedVariable.setValue(methodCallHolder.getVariableName());
        accessedVariable.setVariableType(methodType);
        return true;
    }

    private Boolean visitNewArray(JmmNode node, SubstituteVariable substituteVariable) {
        Type arrayType = new Type("int", true);
        Type elementType = new Type("int", false);

        // Visit indexation
        SubstituteVariable positionChild = createTemporaryVariable(node);
        visit(node.getJmmChild(0), positionChild);

        // Hold indexation child in variable
        startNewLine();
        SubstituteVariable sizeHolder = createTemporaryVariable(node);
        ollirCode.append(createTemporaryAssign(sizeHolder.getVariableName(),
                getOllirType(elementType), positionChild.getSubstitute()));

        // Create new array
        startNewLine();
        ollirCode.append(createTemporaryAssign(substituteVariable.getVariableName(), getOllirType(arrayType),
                "new(array, " + sizeHolder.getSubstitute() + ")." + getOllirType(arrayType)));
        substituteVariable.setVariableType(arrayType);

        return true;
    }

    private Boolean visitNewObject(JmmNode node, SubstituteVariable substituteVariable) {
        startNewLine();
        String className = node.get("class");
        ollirCode.append(createTemporaryAssign(substituteVariable.getVariableName(), className,
                "new(" + className + ")." + className));
        substituteVariable.setVariableType(new Type(className, false));
        return true;
    }

    private Boolean visitIdentifier(JmmNode node, SubstituteVariable substituteVariable) {
        String variableName = node.get("name");
        Symbol symbol;
        if (symbolTable.getClosestSymbol(node, variableName).isPresent()){
            symbol = symbolTable.getClosestSymbol(node, variableName).get();
            substituteVariable.setVariableType(symbol.getType());
        } else {
            symbol = new Symbol(new Type("void", false), variableName);
        }

        var closestMethod = getClosestMethod(node);
        if (closestMethod.isEmpty()){
            return false;
        }

        if (symbolTable.getFields().stream().anyMatch(s -> s.getName().equals(variableName))) {
            SubstituteVariable temporaryHolder = createTemporaryVariable(node);
            startNewLine();
            ollirCode.append(createTemporaryAssign(temporaryHolder.getVariableName(), getOllirType(symbol),
                    getField("this", variableName, symbolTable.getClassName())));
            substituteVariable.setValue(temporaryHolder.getVariableName());
            return true;
        }

        var methodName = getMethodName(closestMethod.get());
        boolean isParameter = false;
        int parameterNumber = methodName.equals("main") ? 0 : 1;
        for (var param : symbolTable.getParameters(methodName)){
            if (param.getName().equals(variableName)){
                isParameter = true;
                break;
            }
            parameterNumber += 1;
        }

        substituteVariable.setVariableName(variableName);
        substituteVariable.setValue((isParameter ? "$" + parameterNumber + "." : "") + variableName);
        return true;
    }

    private Boolean visitCompoundExpression(JmmNode node, SubstituteVariable temporaryVariable) {
        if (temporaryVariable == null) {
            temporaryVariable = createTemporaryVariable(node);
        }
        for (var child : node.getChildren()) {
            visit(child, temporaryVariable);
        }
        return true;
    }

    private Boolean visitIntLiteral(JmmNode node, SubstituteVariable substituteVariable) {
        substituteVariable.setValue(node.get("value"));
        substituteVariable.setVariableType(new Type("int", false));
        return true;
    }

    private Boolean visitBooleanLiteral(JmmNode node, SubstituteVariable substituteVariable) {
        substituteVariable.setValue(node.get("value"));
        substituteVariable.setVariableType(new Type("boolean", false));
        return true;
    }

    private Boolean visitThisLiteral(JmmNode node, SubstituteVariable substituteVariable) {
        Type classType = new Type(symbolTable.getClassName(), false);
        substituteVariable.setValue(node.getAncestor("CompoundExpression").isEmpty() ? "$0.this" : "this");
        substituteVariable.setVariableType(classType);
        return true;
    }

    private Boolean visitReturn(JmmNode node, SubstituteVariable dummy) {
        SubstituteVariable temp = createTemporaryVariable(node);
        visit(node.getJmmChild(0), temp);
        startNewLine();
        ollirCode.append("ret.").append(getOllirType(temp.getVariableType())).append(" ")
            .append(temp.getSubstitute()).append(";");
        return true;
    }

}
