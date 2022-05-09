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
        addVisit("BooleanLiteral", this::visitBoolLiteral);
        addVisit("Identifier", this::visitIdentifier);
        addVisit("UnaryOp", this::visitUnaryOp);
        addVisit("BinOp", this::visitBinOp);
        //addVisit("MethodCall", this::visitMethodCall);

        setDefaultVisit(this::visitDefault);
    }

    private Type visitDefault(JmmNode node, Boolean dummy) {
        for (var child : node.getChildren()) {
            visit(child, dummy);
        }
        return null;
    }

    public Type visitIntLiteral(JmmNode node, Boolean dummy) { return new Type("int",false); }

    public Type visitBoolLiteral(JmmNode node, Boolean dummy) { return new Type("bool",false); }

    public Type visitIdentifier(JmmNode node, Boolean dummy) {
        if (node.getAncestor("MethodBody").isEmpty()) { return null; }

        if (node.getJmmParent().getKind().equals("MethodCall")) {
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

        System.out.println(firstChildType.getName());
        System.out.println(secondChildType.getName());

        switch (node.get("op")) {
            case "assign":
                if ((!firstChildType.getName().equals(secondChildType.getName()) ||
                        firstChildType.isArray() != secondChildType.isArray()) &&
                        !secondChildType.getName().equals("extern")) {
                    addSemanticErrorReport(node,"Type of the assignee must be compatible with the assigned");
                }
                return new Type("", false);

            case "and": case "or":
                if ((!firstChildType.getName().equals("bool") && !firstChildType.isArray()) ||
                        !((secondChildType.getName().equals("bool") && !secondChildType.isArray()) ||
                                secondChildType.getName().equals("extern"))) {
                    addSemanticErrorReport(node,"Types are not compatible with the operation");
                }
                return new Type("bool", false);

            case "lt":
                if ((!firstChildType.getName().equals("int") && !firstChildType.isArray()) ||
                        !((secondChildType.getName().equals("int") && !secondChildType.isArray()) ||
                                secondChildType.getName().equals("extern"))) {
                    addSemanticErrorReport(node,"Types are not compatible with the operation");
                }
                return new Type("bool", false);

            default:
                if ((!firstChildType.getName().equals("int") && !firstChildType.isArray()) ||
                        !((secondChildType.getName().equals("int") && !secondChildType.isArray()) ||
                                secondChildType.getName().equals("extern"))) {
                    addSemanticErrorReport(node,"Types are not compatible with the operation");
                }
                return new Type("int", false);
        }
    }
}
