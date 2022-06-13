package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConstantPropagationVisitor extends AJmmVisitor<Boolean, Boolean> {
    private final Map<String, JmmNode> constantAssigns = new HashMap<>();

    public ConstantPropagationVisitor() {
        addVisit("MethodBody", this::visitMethodBody);
        addVisit("While", this::visitWhile);
        addVisit("Identifier", this::visitIdentifier);
        addVisit("BinOp", this::visitBinOp);
        setDefaultVisit(this::visitAllChildren);
    }

    private Boolean visitWhile(JmmNode node, Boolean dummy) {
        visit(node.getJmmChild(1));
        visit(node.getJmmChild(0));
        return true;
    }

    private Boolean visitMethodBody(JmmNode node, Boolean dummy) {
        constantAssigns.clear();
        visitAllChildren(node, dummy);
        return true;
    }

    private Boolean visitIdentifier(JmmNode node, Boolean dummy) {
        if (node.getAncestor("MethodBody").isEmpty() && node.getAncestor("Return").isEmpty()) {
            return false;
        }

        Optional<JmmNode> binOpAncestor = node.getAncestor("BinOp");
        while (binOpAncestor.isPresent() && !binOpAncestor.get().get("op").equals("assign")) {
            binOpAncestor = binOpAncestor.get().getAncestor("BinOp");
        }
        boolean isAssignLhs = binOpAncestor.isPresent()
                && binOpAncestor.get().get("op").equals("assign")
                && binOpAncestor.get().getJmmChild(0).get("name").equals(node.get("name"));
        boolean isVarDeclaration = node.getJmmParent().getKind().equals("VarDeclaration")
                && node.getJmmParent().getJmmChild(1).get("name").equals(node.get("name"));
        if (isAssignLhs || isVarDeclaration) {
            return false;
        }
        if (constantAssigns.containsKey(node.get("name"))) {
            JmmNode replaceCopy = JmmNode.fromJson(constantAssigns.get(node.get("name")).toJson());
            node.replace(replaceCopy);
        }
        return true;
    }

    private Boolean visitBinOp(JmmNode node, Boolean dummy) {
        visitAllChildren(node, dummy);
        JmmNode lhs = node.getJmmChild(0);
        if (!node.get("op").equals("assign") || !lhs.getKind().equals("Identifier")) {
            return false;
        }
        JmmNode rhs = node.getJmmChild(1);
        if ((!rhs.getKind().equals("IntLiteral") && !rhs.getKind().equals("BooleanLiteral")) || node.getChildren().size() > 2) {
            constantAssigns.remove(lhs.get("name"));
            return false;
        } else {
            constantAssigns.put(lhs.get("name"), rhs);
            return true;
        }
    }
}
