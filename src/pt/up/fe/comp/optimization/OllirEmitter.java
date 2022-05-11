package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

import static pt.up.fe.comp.optimization.OllirUtils.getOllirType;
import static pt.up.fe.comp.semantic.tables.JmmSymbolTable.getClosestMethod;
import static pt.up.fe.comp.semantic.tables.JmmSymbolTable.getMethodName;

public class OllirEmitter extends AJmmVisitor<SubstituteVariable, String> {
    private final StringBuilder ollirCode;
    private final JmmSymbolTable symbolTable;
    private int temporaryVariableCounter = -1;

    public OllirEmitter(StringBuilder ollirCode, JmmSymbolTable symbolTable) {
        this.ollirCode = ollirCode;
        this.symbolTable = symbolTable;
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("VarDeclaration", this::visitFieldDeclaration);
        addVisit("RegularMethod", this::visitMethod);
        addVisit("MainMethod", this::visitMethod);
        addVisit("MethodBody", this::visitMethodBody);
        addVisit("BinOp", this::visitBinOp);
        addVisit("CompoundExpression", this::visitCompoundExpression);
        addVisit("IntLiteral", this::visitIntLiteral);
        setDefaultVisit((node, dummy) -> null);
    }

    private SubstituteVariable createTemporaryVariable() {
        temporaryVariableCounter += 1;
        return new SubstituteVariable().withVariableName("_temp" + temporaryVariableCounter);
    }

    private void fillImports() {
        for (var imp : symbolTable.getImports()) {
            ollirCode.append("import ").append(imp).append(";\n");
        }
    }

    private String visitStart(JmmNode node, SubstituteVariable dummy) {
        fillImports();
        visitAllChildren(node, dummy);
        return null;
    }

    private String visitClassDeclaration(JmmNode node, SubstituteVariable dummy) {
        String className = node.get("name");
        ollirCode.append(className).append(" ");
        if (symbolTable.getSuper() != null) {
            ollirCode.append("extends ").append(symbolTable.getSuper()).append(" ");
        }
        ollirCode.append("{\n");
        for (var child : node.getChildren().stream().filter((n) -> n.getKind().equals("VarDeclaration")).collect(Collectors.toList())) {
            ollirCode.append(".field private ");
            visit(child);
        }
        addVisit("VarDeclaration", (n, d) -> null);
        ollirCode.append(OllirUtils.defaultConstructor(className));
        for (var child : node.getChildren().stream().filter((n) -> n.getKind().equals("MainMethod")).collect(Collectors.toList())) {
            ollirCode.append(".method public static ");
            visit(child);
        }
        for (var child : node.getChildren().stream().filter((n) -> n.getKind().equals("RegularMethod")).collect(Collectors.toList())) {
            ollirCode.append(".method public ");
            visit(child);
        }
        ollirCode.append("}\n");
        return null;
    }

    private String visitFieldDeclaration(JmmNode node, SubstituteVariable dummy) {
        String variableName = node.getChildren().get(1).get("name");
        Type type = new Type(node.getChildren().get(0).get("name"), Boolean.parseBoolean(node.getChildren().get(0).get("isArray")));
        ollirCode.append(variableName).append(".").append(getOllirType(type));
        ollirCode.append(";\n");
        return null;
    }

    private String visitMethod(JmmNode node, SubstituteVariable dummy) {
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
        ollirCode.append(").").append(getOllirType(returnType)).append(" {\n");
        for (var child : node.getChildren()){
            if (child.getKind().equals("MethodBody")){
                visit(child);
                break;
            }
        }
        ollirCode.append("}\n");
        return null;
    }

    private String visitMethodBody(JmmNode node, SubstituteVariable dummy) {
        for (var child : node.getChildren()){
            String code = visit(child);
            if (code != null){
                ollirCode.append(code);
            }
        }
        return null;
    }

    private String visitCompoundExpression(JmmNode node, SubstituteVariable dummy) {
        if (!node.getChildren().get(0).getKind().equals("Identifier")) {
            return null;
        }
        var compoundType = node.getChildren().get(1).getKind();
        if (compoundType.equals("MethodCall")) {
            var className = node.getChildren().get(0).get("name");
            var methodName = node.getChildren().get(1).getChildren().get(0).get("name");
            // assume no arguments for now
            ollirCode.append(OllirUtils.invokestatic(className, methodName, new ArrayList<>())).append("\n");
            return null;
        }
        return null;
    }

    private String visitBinOp(JmmNode node, SubstituteVariable temp) {
        var t2 = createTemporaryVariable();
        String rhs = visit(node.getJmmChild(1), t2);

        String operation = node.get("op");
        if (operation.equals("assign")) {
            String variableName = node.getJmmChild(0).get("name");
            Symbol symbol = symbolTable.getClosestSymbol(node, variableName).get();
            String ollirType = getOllirType(symbol);
            ollirCode.append(rhs);
            ollirCode.append(variableName).append(".").append(ollirType)
                    .append(" :=.").append(ollirType).append(" ").append(t2.getVariableName()).append(";\n");
            return null;
        } else {
            String operationCode = "";
            var t1 = createTemporaryVariable();
            String lhs = visit(node.getJmmChild(0), t1);
            String code = temp + " = " + t1.getVariableName() + " " + operationCode + " " + t2;
            return lhs + "\n" + rhs + "\n" + code;
        }
    }

    private String visitIdentifier(JmmNode node, SubstituteVariable substituteVariable) {
        String variableName = node.get("name");
        var closestMethod = getClosestMethod(node);
        if (closestMethod.isEmpty()){
            return null;
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

        Symbol symbol = symbolTable.getClosestSymbol(node, variableName).get();
        String ollirType = getOllirType(symbol);
        substituteVariable.setVariableName(isParameter ? "$" + parameterNumber + "." : ""
                + variableName + "." + getOllirType(symbol) + " :=." + ollirType + " ");
        return "";
    }

    String visitIntLiteral(JmmNode n, SubstituteVariable d) {
        d.setVariableName(n.get("value") + ".i32");
        return "";
    }

}
