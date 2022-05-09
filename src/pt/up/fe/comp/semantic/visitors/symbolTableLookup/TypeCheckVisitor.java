package pt.up.fe.comp.semantic.visitors.symbolTableLookup;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;
import pt.up.fe.comp.semantic.visitors.ReportCollectorJmmNodeVisitor;

import java.util.List;
import java.util.Optional;

public class TypeCheckVisitor extends ReportCollectorJmmNodeVisitor<Boolean,Type> {

    JmmSymbolTable symbolTable;

    public TypeCheckVisitor(JmmSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("IntLiteral", this::visitIntLiteral);
        addVisit("BoolLiteral", this::visitBoolLiteral);
        addVisit("Identifier", this::visitIdentifier);
        addVisit("UnaryOp", this::visitUnaryOp);

        setDefaultVisit(this::visitDefault);
    }

    private Type visitDefault(JmmNode node, Boolean dummy) {
        for (var child : node.getChildren()) {
            visit(child, dummy);
        }
        return null;
    }

    public Type visitIntLiteral(JmmNode node, Boolean dummy) { return new Type("int",false); }

    public Type visitBoolLiteral(JmmNode node, Boolean dummy) {
        return new Type("bool",false);
    }

    public Type visitIdentifier(JmmNode node, Boolean dummy) {
        if (node.getAncestor("MethodBody").isEmpty()) { return null; }

        if (node.getJmmParent().getKind().equals("MethodCall")) {
            System.out.println("entrei1");
            if (isThisMethodCall(node.getJmmParent()) && symbolTable.getMethodsTable().contains(node.get("name"))) {
                List<String> methods = symbolTable.getMethodsTable();
                for (String method : methods) {
                    if (node.get("name").equals(method)) {
                        return symbolTable.getReturnType(method);
                    }
                }
            } else if (isThisMethodCall(node.getJmmParent()) && symbolTable.getSuper() != null) {
                return new Type("extern",false);
            } else if (!isThisMethodCall(node.getJmmParent())) {
                return new Type("extern",false);
            }
        } else {
            Optional<Symbol> child = symbolTable.getClosestSymbol(node, node.get("name"));
            if (child.isPresent()) {
                return child.get().getType();
            }
        }

        addSemanticErrorReport(node, "Identifier " + node.get("name") + " does not exists");
        return new Type("", false);
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

    public Type visitUnaryOp(JmmNode node, Boolean dummy) {
        Type childType = visit(node.getChildren().get(0), dummy);

        if ((childType.getName().equals("bool") && !childType.isArray()) || childType.getName().equals("extern")) {
            return childType;
        }

        addSemanticErrorReport(node, "Incompatible types. Not operation expects a bool");
        return new Type("", false);
    }

    public Type visitBinOp(JmmNode node, Boolean dummy) {
        Type firstChildType = visit(node.getChildren().get(0), dummy);
        Type secondChildType = visit(node.getChildren().get(1), dummy);

        return null;
    }
}
