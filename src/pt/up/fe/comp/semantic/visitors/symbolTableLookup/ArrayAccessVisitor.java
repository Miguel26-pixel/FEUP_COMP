package pt.up.fe.comp.semantic.visitors.symbolTableLookup;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;
import pt.up.fe.comp.semantic.visitors.SemanticJmmNodeVisitor;

public class ArrayAccessVisitor extends SemanticJmmNodeVisitor {

    public ArrayAccessVisitor(JmmSymbolTable symbolTable) {
        super(symbolTable);
        addVisit("ArrayElement", this::visitArrayElement);
    }

    private Boolean visitArrayElement(JmmNode node, Boolean dummy) {
        var closestVariable = symbolTable.getClosestSymbol(node, node.getChildren().get(0).get("name"));
        if (closestVariable.isEmpty()) {
            return false;
        }
        if (!closestVariable.get().getType().isArray()) {
            addSemanticErrorReport(node, "Array access is not done over an array");
            return false;
        }
        return true;
    }
}
