package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;

import java.util.stream.Collectors;

public class OllirEmitter extends AJmmVisitor<Boolean, Boolean> {
    private final StringBuilder ollirCode;
    private final JmmSymbolTable symbolTable;

    public OllirEmitter(StringBuilder ollirCode, JmmSymbolTable symbolTable) {
        this.ollirCode = ollirCode;
        this.symbolTable = symbolTable;
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("VarDeclaration", this::visitVarDeclaration);
        addVisit("RegularMethod", this::visitMethod);
        addVisit("MainMethod", this::visitMethod);
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

    private Boolean visitVarDeclaration(JmmNode node, Boolean dummy) {
        String variableName = node.getChildren().get(1).get("name");
        var symbol = symbolTable.getFields().stream().
                filter((s) -> s.getName().equals(variableName)).collect(Collectors.toList()).get(0);
        ollirCode.append(variableName).append(".").append(OllirUtils.getOllirType(symbol));
        /*if (node.getChildren().size() > 2) {
            ollirCode.append(" :=").append("").append(" 0");
        }*/
        ollirCode.append(";\n");
        return true;
    }

    private Boolean visitMethod(JmmNode node, Boolean dummy) {
        String methodName = node.getKind().equals("RegularMethod") ? node.getChildren().get(1).get("name") : "main";
        var returnType = symbolTable.getReturnType(methodName);
        ollirCode.append(methodName).append("(");
        for (var parameter : symbolTable.getParameters(methodName)) {
            ollirCode.append(parameter.getName()).append(".").append(OllirUtils.getOllirType(parameter)).append(", ");
        }
        ollirCode.delete(ollirCode.lastIndexOf(","), ollirCode.length());
        ollirCode.append(").").append(OllirUtils.getOllirType(returnType)).append(" {\n");
        ollirCode.append("}\n");
        return true;
    }

}
