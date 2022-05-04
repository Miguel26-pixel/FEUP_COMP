package pt.up.fe.comp.semantic.visitors;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;

public abstract class SemanticJmmNodeVisitor extends ReportCollectorJmmNodeVisitor<Boolean, Boolean> {
    protected final JmmSymbolTable symbolTable;

    public SemanticJmmNodeVisitor(JmmSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        setDefaultVisit(this::visitDefault);
    }

    private Boolean visitDefault(JmmNode node, Boolean dummy) {
        for (var child : node.getChildren()) {
            visit(child, dummy);
        }
        return true;
    }

}
