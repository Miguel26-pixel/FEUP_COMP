package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;

import java.util.ArrayList;
import java.util.stream.Collectors;

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
        ollirCode.append("\n").append(" ".repeat(numberOfSpaces * indentationLevel));
    }

    private String createTemporaryAssign(SubstituteVariable substituteVariable, String value) {
        String ollirType = getOllirType(substituteVariable.getVariableType());
        if (ollirType.equals("V")) {
            return value + ";";
        }
        return substituteVariable.getVariableName() + "." + ollirType + " :=." + ollirType + " " + value + ";";
    }

    private SubstituteVariable createTemporaryVariable(JmmNode closestNode) {
        temporaryVariableCounter += 1;
        String name = "t" + temporaryVariableCounter;
        while (symbolTable.getClosestSymbol(closestNode, name).isPresent()) {
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
        return true;
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
        return true;
    }

    private Boolean visitFieldDeclaration(JmmNode node, SubstituteVariable dummy) {
        String variableName = node.getChildren().get(1).get("name");
        Type type = new Type(node.getChildren().get(0).get("name"), Boolean.parseBoolean(node.getChildren().get(0).get("isArray")));
        ollirCode.append(variableName).append(".").append(getOllirType(type));
        ollirCode.append(";");
        return true;
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
        boolean returned = false;
        for (var child : node.getChildren()) {
            if (child.getKind().equals("Return")) {
                visit(child);
                returned = true;
            } else if (child.getKind().equals("MethodBody")) {
                visit(child);
            }
        }
        if (!returned) {
            startNewLine();
            ollirCode.append("ret.").append(getOllirType(returnType)).append(";");
        }
        indentationLevel--;
        startNewLine();
        ollirCode.append("}");
        return true;
    }

    private Boolean visitIf(JmmNode node, SubstituteVariable dummy) {
        ifCounter++;
        int localCounter = ifCounter;

        SubstituteVariable conditionHolder = createTemporaryVariable(node);
        visit(node.getJmmChild(0), conditionHolder);
        startNewLine();
        ollirCode.append("if (!.bool ").append(conditionHolder.getSubstituteWithType())
                .append(") goto Else").append(localCounter).append(";");
        startNewLine();
        ollirCode.append("Then").append(localCounter).append(":");
        indentationLevel++;
        for (var thenChild : node.getJmmChild(1).getChildren()) {
            visit(thenChild);
        }
        startNewLine();
        ollirCode.append("goto EndIf").append(localCounter).append(";");
        indentationLevel--;
        startNewLine();
        ollirCode.append("Else").append(localCounter).append(":");
        indentationLevel++;
        for (var elseChild : node.getJmmChild(2).getChildren()) {
            visit(elseChild);
        }
        indentationLevel--;
        startNewLine();
        ollirCode.append("EndIf").append(localCounter).append(":");
        return true;
    }

    private Boolean visitWhile(JmmNode node, SubstituteVariable dummy) {
        loopCounter++;
        int localCounter = loopCounter;

        startNewLine();
        ollirCode.append("Loop").append(localCounter).append(":");
        indentationLevel++;
        SubstituteVariable conditionHolder = createTemporaryVariable(node);
        visit(node.getJmmChild(0), conditionHolder);
        startNewLine();
        ollirCode.append("if (!.bool ").append(conditionHolder.getSubstituteWithType())
                .append(") goto EndLoop").append(localCounter).append(";");
        for (var bodyChild : node.getJmmChild(1).getChildren()) {
            visit(bodyChild);
        }
        startNewLine();
        ollirCode.append("goto Loop").append(localCounter).append(";");
        indentationLevel--;
        startNewLine();
        ollirCode.append("EndLoop").append(localCounter).append(":");
        return true;
    }

    private Boolean visitBinOp(JmmNode node, SubstituteVariable substituteVariable) {
        var t1 = createTemporaryVariable(node);
        var t2 = createTemporaryVariable(node);
        visit(node.getJmmChild(0), t1);
        t2.setAssignType(t1.getVariableType());
        visit(node.getJmmChild(1), t2);

        String operation = node.get("op");
        if (operation.equals("assign")) {
            String ollirType = getOllirType(t1.getVariableType());
            var closestMethod = getClosestMethod(node);
            boolean isClassField = symbolTable.getFields().stream().anyMatch(s -> s.getName().equals(t1.getVariableName())) && closestMethod.isPresent()
                    && symbolTable.getLocalVariables(getMethodName(closestMethod.get())).stream().noneMatch(s -> s.getName().equals(t1.getVariableName()));
            startNewLine();
            if (isClassField) {
                ollirCode.append(OllirUtils.putField("this", t1.getSubstituteWithType(), t2.getSubstituteWithType())).append(";");
            } else {
                ollirCode.append(t1.getSubstituteWithType()).append(" :=.").append(ollirType)
                        .append(" ").append(t2.getSubstituteWithType()).append(";");
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
            String code = t1.getSubstituteWithType() + " " + operationCode + "." + tempType + " " + t2.getSubstituteWithType();
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
            substituteVariable.setVariableType(childHolder.getVariableType());
            startNewLine();
            ollirCode.append(createTemporaryAssign(substituteVariable, "!.bool " + childHolder.getSubstituteWithType()));
            substituteVariable.setVariableType(childHolder.getVariableType());
        }
        return true;
    }

    private Boolean visitArrayElement(JmmNode node, SubstituteVariable substituteVariable) {
        String variableName = node.getJmmChild(0).get("name");
        Symbol symbol;
        if (symbolTable.getClosestSymbol(node.getJmmChild(0), variableName).isPresent()) {
            symbol = symbolTable.getClosestSymbol(node.getJmmChild(0), variableName).get();
        } else {
            symbol = new Symbol(new Type("void", false), variableName);
        }

        // Visit indexation
        SubstituteVariable positionChild = createTemporaryVariable(node);
        visit(node.getJmmChild(1), positionChild);

        // Get element type
        substituteVariable.setVariableName(variableName);
        Type elementType = new Type(symbol.getType().getName(), false);

        // Hold indexation in variable
        startNewLine();
        SubstituteVariable positionTemporaryVariable = createTemporaryVariable(node);
        positionTemporaryVariable.setVariableType(elementType);
        ollirCode.append(createTemporaryAssign(positionTemporaryVariable, positionChild.getSubstituteWithType()));
        var closestMethod = getClosestMethod(node);
        boolean isClassField = symbolTable.getFields().stream().anyMatch(s -> s.getName().equals(variableName) && closestMethod.isPresent())
                && symbolTable.getLocalVariables(getMethodName(closestMethod.get())).stream().noneMatch(s -> s.getName().equals(variableName));
        if (isClassField) {
            SubstituteVariable referenceHolder = createTemporaryVariable(node);
            referenceHolder.setVariableType(elementType);
            startNewLine();
            ollirCode.append(createTemporaryAssign(referenceHolder, getField("this", variableName, getOllirType(elementType))));
            substituteVariable.setVariableName(referenceHolder.getVariableName() + "[" + positionTemporaryVariable.getSubstituteWithType() + "]");
        } else {
            substituteVariable.setVariableName(variableName + "[" + positionTemporaryVariable.getSubstituteWithType() + "]");
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
        positionTemporaryVariable.setVariableType(positionChild.getVariableType());
        ollirCode.append(createTemporaryAssign(positionTemporaryVariable, positionChild.getSubstituteWithType()));

        // Store indexed variable in temporary variable
        startNewLine();
        SubstituteVariable indexVariable = createTemporaryVariable(node);
        indexVariable.setVariableType(elementType);
        ollirCode.append(createTemporaryAssign(indexVariable,
                accessedVariable.getValue() + "[" + positionTemporaryVariable.getSubstituteWithType() + "]." + getOllirType(elementType)));
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
            lengthHolder.setVariableType(new Type("int", false));
            ollirCode.append(createTemporaryAssign(lengthHolder, arrayLength(accessedVariable.getValue(), "i32")));
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
        attributeVariable.setVariableType(attributeChild.getVariableType());
        ollirCode.append(createTemporaryAssign(attributeVariable, getField(accessedVariable.getInvokeString(node, symbolTable),
                attributeChild.getVariableName(), getOllirType(elementType))));
        accessedVariable.setValue(attributeVariable.getVariableName());
        accessedVariable.setVariableType(elementType);

        return true;
    }

    private Boolean visitMethodCall(JmmNode node, SubstituteVariable accessedVariable) {
        if (accessedVariable == null) {
            return false;
        }

        // Visit arguments
        var arguments = new ArrayList<SubstituteVariable>();
        for (var arg : node.getJmmChild(1).getChildren()) {
            SubstituteVariable methodArgument = createTemporaryVariable(node);
            visit(arg, methodArgument);
            arguments.add(methodArgument);
        }

        // Write method call
        startNewLine();
        String methodName = node.getJmmChild(0).get("name");
        Type methodType = symbolTable.getReturnType(methodName);
        if (methodType == null) {
            methodType = accessedVariable.getAssignType();
        }
        String ollirMethodType = getOllirType(methodType);
        SubstituteVariable methodCallHolder = createTemporaryVariable(node);
        methodCallHolder.setVariableType(methodType);

        boolean isVirtualCall = accessedVariable.getValue().equals("this") || symbolTable.isLocalVariable(node, accessedVariable.getSubstitute());

        SubstituteVariable targetHolder = isVirtualCall ? createTemporaryVariable(node) : accessedVariable;
        if (isVirtualCall) {
            targetHolder.setVariableType(accessedVariable.getVariableType());
            ollirCode.append(createTemporaryAssign(targetHolder, accessedVariable.getSubstituteWithType()));
            targetHolder.setVariableType(accessedVariable.getVariableType());
            startNewLine();
        }

        methodCallHolder.setVariableType(methodType);
        ollirCode.append(createTemporaryAssign(methodCallHolder,
                invoke(isVirtualCall ? "invokevirtual" : "invokestatic", targetHolder.getInvokeString(node, symbolTable),
                        node.getJmmChild(0).get("name"),
                        arguments.stream().map(SubstituteVariable::getSubstituteWithType).collect(Collectors.toList()), ollirMethodType)));

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
        sizeHolder.setVariableType(elementType);
        ollirCode.append(createTemporaryAssign(sizeHolder, positionChild.getSubstituteWithType()));

        // Create new array
        startNewLine();
        substituteVariable.setVariableType(arrayType);
        ollirCode.append(createTemporaryAssign(substituteVariable,
                "new(array, " + sizeHolder.getSubstituteWithType() + ")." + getOllirType(arrayType)));
        substituteVariable.setVariableType(arrayType);

        return true;
    }

    private Boolean visitNewObject(JmmNode node, SubstituteVariable substituteVariable) {
        startNewLine();
        String className = node.get("class");
        substituteVariable.setVariableType(new Type(className, false));
        ollirCode.append(createTemporaryAssign(substituteVariable,
                "new(" + className + ")." + className));

        startNewLine();
        ollirCode.append(invoke("invokespecial", substituteVariable.getSubstituteWithType(),
                "<init>", new ArrayList<>(), "V")).append(";");

        substituteVariable.setVariableType(new Type(className, false));
        return true;
    }

    private Boolean visitIdentifier(JmmNode node, SubstituteVariable substituteVariable) {
        String variableName = node.get("name");
        Symbol symbol;
        if (symbolTable.getClosestSymbol(node, variableName).isPresent()) {
            symbol = symbolTable.getClosestSymbol(node, variableName).get();
            substituteVariable.setVariableType(symbol.getType());
            substituteVariable.setVariableName(symbol.getName());
        } else {
            symbol = new Symbol(new Type("void", false), variableName);
            substituteVariable.setVariableType(symbol.getType());
        }

        var closestMethod = getClosestMethod(node);
        if (closestMethod.isEmpty()) {
            return false;
        }

        var methodName = getMethodName(closestMethod.get());
        boolean isParameter = false;
        int parameterNumber = methodName.equals("main") ? 0 : 1;
        for (var param : symbolTable.getParameters(methodName)) {
            if (param.getName().equals(variableName)) {
                isParameter = true;
                break;
            }
            parameterNumber += 1;
        }

        boolean isClassField = symbolTable.getFields().stream().anyMatch(s -> s.getName().equals(variableName))
                && symbolTable.getLocalVariables(methodName).stream().noneMatch(s -> s.getName().equals(variableName));
        boolean isAssign = node.getAncestor("BinOp").isPresent()
                && node.getAncestor("BinOp").get().get("op").equals("assign")
                && node.getAncestor("BinOp").get().getJmmChild(0).equals(node);
        if (isClassField && !isAssign) {
            startNewLine();
            SubstituteVariable tempHolder = createTemporaryVariable(node);
            tempHolder.setVariableType(symbol.getType());
            ollirCode.append(createTemporaryAssign(tempHolder,
                    getField("this", variableName, getOllirType(symbol.getType()))));
            substituteVariable.setVariableName(tempHolder.getVariableName());
            substituteVariable.setValue(getSafeVariableName(tempHolder.getVariableName()));
        } else {
            substituteVariable.setVariableName(variableName);
            substituteVariable.setValue((isParameter ? "$" + parameterNumber + "." : "") + getSafeVariableName(variableName));
        }

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
        substituteVariable.setValue(node.get("value").equals("true") ? "1" : "0");
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
        if (getClosestMethod(node).isPresent()) {
            String methodName = getClosestMethod(node).get().getKind().equals("RegularMethod") ?
                    getClosestMethod(node).get().getJmmChild(1).get("name") : "main";
            temp.setVariableType(symbolTable.getReturnType(methodName));
        }
        visit(node.getJmmChild(0), temp);
        startNewLine();
        ollirCode.append("ret.").append(getOllirType(temp.getVariableType())).append(" ")
                .append(temp.getSubstituteWithType()).append(";");
        return true;
    }

}
