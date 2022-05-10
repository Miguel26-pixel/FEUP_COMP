package pt.up.fe.comp.semantic.visitors.symbolTableLookup;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;
import pt.up.fe.comp.semantic.visitors.ReportCollectorJmmNodeVisitor;

public class FunctionCheckVisitor extends ReportCollectorJmmNodeVisitor<Type, Type> {

    JmmSymbolTable symbolTable;

    public FunctionCheckVisitor(JmmSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("Identifier",this::visitIdentifier);
        setDefaultVisit(this::visitDefault);
    }

    private Type visitDefault(JmmNode node, Type dummy) {
        for (var child : node.getChildren()) {
            visit(child, dummy);
        }
        return null;
    }

    private Type visitIdentifier(JmmNode node, Type dummy) {
        return new Type("", false);
    }
}
