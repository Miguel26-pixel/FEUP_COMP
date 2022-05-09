package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class OllirEmitter extends AJmmVisitor<Boolean, Boolean> {
    private final StringBuilder ollirCode;
    private final JmmSymbolTable symbolTable;

    public OllirEmitter(StringBuilder ollirCode, JmmSymbolTable symbolTable) {
        this.ollirCode = ollirCode;
        this.symbolTable = symbolTable;
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("VarDeclaration", this::visitFieldDeclaration);
        addVisit("RegularMethod", this::visitMethod);
        addVisit("MainMethod", this::visitMethod);
        addVisit("MethodBody", this::visitAllChildren);
        addVisit("CompoundExpression", this::visitCompoundExpression);
        setDefaultVisit((node, dummy) -> true);
    }

    private void fillImports() {
        for (var imp : symbolTable.getImports()) {
            ollirCode.append("import ").append(imp).append(";\n");
        }
    }

    private Boolean visitStart(JmmNode node, Boolean dummy) {
        fillImports();
        visitAllChildren(node, dummy);
        return true;
    }

    private Boolean visitClassDeclaration(JmmNode node, Boolean dummy) {
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
        addVisit("VarDeclaration", (n, d) -> true);
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
        return true;
    }

    private Boolean visitFieldDeclaration(JmmNode node, Boolean dummy) {
        String variableName = node.getChildren().get(1).get("name");
        Type type = new Type(node.getChildren().get(0).get("name"), Boolean.parseBoolean(node.getChildren().get(0).get("isArray")));
        ollirCode.append(variableName).append(".").append(OllirUtils.getOllirType(type));
        ollirCode.append(";\n");
        return true;
    }

    private Boolean visitMethod(JmmNode node, Boolean dummy) {
        String methodName = node.getKind().equals("RegularMethod") ? node.getChildren().get(1).get("name") : "main";
        var returnType = symbolTable.getReturnType(methodName);
        ollirCode.append(methodName).append("(");
        var parameters = symbolTable.getParameters(methodName);
        if (!parameters.isEmpty()) {
            for (var parameter : symbolTable.getParameters(methodName)) {
                ollirCode.append(parameter.getName()).append(".").append(OllirUtils.getOllirType(parameter)).append(", ");
            }
            ollirCode.delete(ollirCode.lastIndexOf(","), ollirCode.length());
        }
        ollirCode.append(").").append(OllirUtils.getOllirType(returnType)).append(" {\n");
        visitAllChildren(node, dummy);
        ollirCode.append("}\n");
        return true;
    }

    private Boolean visitCompoundExpression(JmmNode node, Boolean dummy) {
        if (!node.getChildren().get(0).getKind().equals("Identifier")) {
            return false; // not implemented yet
        }
        var compoundType = node.getChildren().get(1).getKind();
        if (compoundType.equals("MethodCall")) {
            var className = node.getChildren().get(0).get("name");
            var methodName = node.getChildren().get(1).getChildren().get(0).get("name");
            // assume no arguments for now
            ollirCode.append(OllirUtils.invokestatic(className, methodName, new ArrayList<>())).append("\n");
            return true;
        }
        return false;
    }

}
