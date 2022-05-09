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
        setDefaultVisit((node, dummy) -> true);
    }

    private void fillImports() {
        for (var imp : symbolTable.getImports()) {
            ollirCode.append("import ").append(imp).append(";\n");
        }
    }

    private Boolean visitStart(JmmNode node, Boolean dummy) {
        fillImports();
        for (var child : node.getChildren()) {
            visit(child);
        }
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
            visit(child);
        }
        for (var child : node.getChildren().stream().filter((n) -> n.getKind().equals("RegularMethod")).collect(Collectors.toList())) {
            visit(child);
        }
        ollirCode.append("}\n");
        return true;
    }

}
