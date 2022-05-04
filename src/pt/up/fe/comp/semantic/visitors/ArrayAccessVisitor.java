package pt.up.fe.comp.semantic.visitors;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;

public class ArrayAccessVisitor extends SemanticJmmNodeVisitor {

    public ArrayAccessVisitor(JmmSymbolTable symbolTable) {
        super(symbolTable);
        addVisit("Indexation", this::visitIndexation);
    }

    private Boolean visitIndexation(JmmNode node, Boolean dummy) {
        var closestVariable = symbolTable.getClosestSymbol(node, node.getChildren().get(0).get("name"));
        if (closestVariable.isEmpty()) {
            return false;
        }
        if (!closestVariable.get().getType().isArray()) {
            int line = Integer.parseInt(node.get("line"));
            addSemanticErrorReport(line, "Array access on line " + line + " is not done over an array");
            return false;
        }
        return true;
    }
}
