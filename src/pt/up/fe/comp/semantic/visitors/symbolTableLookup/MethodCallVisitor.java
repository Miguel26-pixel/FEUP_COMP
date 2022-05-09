package pt.up.fe.comp.semantic.visitors.symbolTableLookup;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;
import pt.up.fe.comp.semantic.visitors.SemanticJmmNodeVisitor;

import java.util.Optional;

public class MethodCallVisitor extends SemanticJmmNodeVisitor {
    public MethodCallVisitor(JmmSymbolTable symbolTable) {
        super(symbolTable);
        addVisit("MethodCall", this::visitMethodCall);
    }

    private Boolean visitMethodCall(JmmNode node, Boolean dummy) {
        String methodName = node.getChildren().get(0).get("name");
        if (isThisMethodCall(node) && symbolTable.getSuper() == null && !symbolTable.getMethodsTable().contains(methodName)) {
            addSemanticErrorReport(node, "Call to a non existing method: " + methodName);
            return false;
        }
        return true;
    }

    private Boolean isThisMethodCall(JmmNode methodCall) {
        Optional<JmmNode> compoundExpression = methodCall.getAncestor("CompoundExpression");
        if (compoundExpression.isPresent()) {
            JmmNode child = compoundExpression.get().getChildren().get(0);

            if (child.getKind().equals("ThisLiteral")) { return true; }

            Optional<Symbol> closest = symbolTable.getClosestSymbol(child, child.get("name"));
            if (closest.isPresent()) {
                return closest.get().getType().getName().equals(symbolTable.getClassName());
            }
        }
        return false;
    }
}
