package pt.up.fe.comp.semantic.visitors.symbolTableLookup;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.helpers.BinOp;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;
import pt.up.fe.comp.semantic.visitors.SemanticJmmNodeVisitor;

public class OperandsVisitor extends SemanticJmmNodeVisitor {
    public OperandsVisitor(JmmSymbolTable symbolTable) {
        super(symbolTable);
        addVisit("BinOp", this::visitBinOp);
    }

    private Boolean visitBinOp(JmmNode node, Boolean dummy) {
        BinOp binop = new BinOp(node, this.symbolTable);
        if (binop.getOperationTypeError()) {
            addSemanticErrorReport(node, "Operands must have compatible types with the operantion");
        }
        if (binop.getAssignTypeError()) {
            addSemanticErrorReport(node, "Assignee must have compatible type with the assigned");
        }
        if (binop.getArrayOperationError()) {
            addSemanticErrorReport(node, "Array cannot be used in arithmetic operations");
        }
        return true;
    }
}
