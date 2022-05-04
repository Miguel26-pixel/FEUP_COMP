package pt.up.fe.comp.semantic.visitors.symbolTableLookup;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;
import pt.up.fe.comp.semantic.visitors.SemanticJmmNodeVisitor;

public class VariableAccessVisitor extends SemanticJmmNodeVisitor {
    public VariableAccessVisitor(JmmSymbolTable symbolTable) {
        super(symbolTable);
        addVisit("Identifier", this::visitIdentifier);
    }

    private Boolean visitIdentifier(JmmNode node, Boolean dummy) {
        if (node.getJmmParent().getKind().equals("ImportDeclaration")) {
            return true;
        } else if (node.getJmmParent().getKind().equals("VarDeclaration")) {
            return true;
        } else if (node.getJmmParent().getKind().equals("Parameter")) {
            return true;
        } else if (node.getJmmParent().getKind().equals("RegularMethod")) {
            return true;
        } else if (node.getJmmParent().getKind().equals("MethodCall")) {
            return true;
        }
        if (symbolTable.getClosestSymbol(node, node.get("name")).isEmpty()) {
            addSemanticErrorReport(node, "Variable " + node.get("name") + " has not been declared");
            return false;
        }
        return true;
    }
}
