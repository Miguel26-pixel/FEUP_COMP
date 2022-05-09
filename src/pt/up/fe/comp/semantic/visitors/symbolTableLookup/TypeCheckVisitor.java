package pt.up.fe.comp.semantic.visitors.symbolTableLookup;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;
import pt.up.fe.comp.semantic.visitors.ReportCollectorJmmNodeVisitor;

import java.util.List;
import java.util.Optional;

public class TypeCheckVisitor extends ReportCollectorJmmNodeVisitor<Type,Type> {

    JmmSymbolTable symbolTable;

    public TypeCheckVisitor(JmmSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("IntLiteral", this::visitIntLiteral);
        addVisit("BooleanLiteral", this::visitBoolLiteral);
        addVisit("Identifier", this::visitIdentifier);
        addVisit("UnaryOp", this::visitUnaryOp);
        addVisit("BinOp", this::visitBinOp);
        addVisit("ArrayElement", this::visitArrayElement);
        addVisit("CompoundExpression", this::visitCompoundExpression);
        addVisit("Indexation", this::visitIndexation);
        //addVisit("MethodCall", this::visitMethodCall);

        setDefaultVisit(this::visitDefault);
    }

    private Type visitDefault(JmmNode node, Type dummy) {
        for (var child : node.getChildren()) {
            visit(child, dummy);
        }
        return null;
    }

    public Type visitIntLiteral(JmmNode node, Type dummy) { return new Type("int",false); }

    public Type visitBoolLiteral(JmmNode node, Type dummy) { return new Type("bool",false); }

    public Type visitIdentifier(JmmNode node, Type dummy) {
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

    public Type visitUnaryOp(JmmNode node, Type dummy) {
        Type childType = visit(node.getChildren().get(0), dummy);

        if ((childType.getName().equals("bool") && !childType.isArray()) || childType.getName().equals("extern")) {
            return childType;
        }

        addSemanticErrorReport(node, "Incompatible types. Not operation expects a bool");
        return new Type("", false);
    }

    public Type visitBinOp(JmmNode node, Type dummy) {
        Type firstChildType = visit(node.getChildren().get(0), dummy);
        Type secondChildType = visit(node.getChildren().get(1), dummy);

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

    public Type visitArrayElement(JmmNode node, Type dummy) {
        //Type firstChildType = visit(node.getChildren().get(0), dummy);
        Type secondChildType = visit(node.getChildren().get(1), dummy);

        if (!secondChildType.getName().equals("int")) {
            addSemanticErrorReport(node, "Array access index must be an expression of type integer");
        }
        return new Type("int", false);
    }

    public Type visitCompoundExpression(JmmNode node, Type dummy) {
        List<JmmNode> children = node.getChildren();
        Type type = dummy;
        for (JmmNode child : children) {
            type = visit(child, type);
            if (type.getName().isEmpty()) { break; }
        }
        return type;
    }

    public Type visitIndexation(JmmNode node, Type type) {
        boolean err = false;
        Type firstChildType = visit(node.getChildren().get(0), type);

        if (!type.getName().equals("extern")) {
            if (!type.isArray()) {
                addSemanticErrorReport(node, "Array access is not done over an array");
                err = true;
            }
            if (!firstChildType.getName().equals("int")) {
                addSemanticErrorReport(node, "Array access index must be an expression of type integer");
                err = true;
            }
            if (err) {
                return new Type("", false);
            }
        }

        return new Type(type.getName(),false); //only work for 1d array
    }
}
