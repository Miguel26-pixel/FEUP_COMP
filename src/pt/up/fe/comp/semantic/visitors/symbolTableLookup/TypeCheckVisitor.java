package pt.up.fe.comp.semantic.visitors.symbolTableLookup;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;
import pt.up.fe.comp.semantic.visitors.ReportCollectorJmmNodeVisitor;
import pt.up.fe.comp.semantic.visitors.SemanticJmmNodeVisitor;

import java.util.Optional;

public class TypeCheckVisitor extends ReportCollectorJmmNodeVisitor<Type,Type> {

    JmmSymbolTable symbolTable;

    public TypeCheckVisitor(JmmSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("IntLiteral", this::visitIntLiteral);
        addVisit("BoolLiteral", this::visitBoolLiteral);
        addVisit("Identifier", this::visitIdentifier);
    }

    public Type visitIntLiteral(JmmNode node, Type dummy) {
        return new Type("int",false);
    }

    public Type visitBoolLiteral(JmmNode node, Type dummy) {
        return new Type("bool",false);
    }

    public Type visitIdentifier(JmmNode node, Type dummy) {
        Optional<Symbol> child = symbolTable.getClosestSymbol(node, node.get("name"));
        if (child.isPresent()) {
            return child.get().getType();
        }
        addSemanticErrorReport(node, "Variable identifier does not exists");
        return new Type("", false);
    }

    public Type visitUnaryOp(JmmNode node, Type type) {
        Type childType = visit(node.getChildren().get(0), type);

        if (childType.getName().equals("bool") && !childType.isArray()) {
            return childType;
        }
        return new Type("", false);
    }



}
