package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;

public class ConstantPropagationVisitor extends AJmmVisitor<Boolean, Boolean> {
    private final Map<String, JmmNode> constantAssigns = new HashMap<>();
    private final Map<String, JmmNode> scheduledAssignRemovals = new HashMap<>();
    private final Map<String, JmmNode> varDeclarations = new HashMap<>();

    public ConstantPropagationVisitor() {
        addVisit("MethodBody", this::visitMethodBody);
        addVisit("While", this::visitWhile);
        addVisit("Identifier", this::visitIdentifier);
        addVisit("BinOp", this::visitBinOp);
        addVisit("VarDeclaration", this::visitVarDeclaration);
        setDefaultVisit(this::visitAllChildren);
    }

    private Boolean visitWhile(JmmNode node, Boolean dummy) {
        visit(node.getJmmChild(1));
        visit(node.getJmmChild(0));
        return true;
    }

    private Boolean visitMethodBody(JmmNode node, Boolean dummy) {
        constantAssigns.clear();
        scheduledAssignRemovals.clear();
        varDeclarations.clear();
        visitAllChildren(node, dummy);
        removeAllConstantAssignsAndVarDeclarations();
        return true;
    }

    private Boolean visitVarDeclaration(JmmNode node, Boolean dummy) {
        JmmNode target = node.getJmmChild(1);
        if (!target.getKind().equals("Identifier")){
            return false;
        }
        varDeclarations.put(target.get("name"), node);
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
                && binOpAncestor.get().getJmmChild(0).getKind().equals("Identifier")
                && binOpAncestor.get().getJmmChild(0).get("name").equals(node.get("name"));
        boolean isVarDeclaration = node.getJmmParent().getKind().equals("VarDeclaration")
                && node.getJmmParent().getJmmChild(1).getKind().equals("Identifier")
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
        String targetName = lhs.get("name");
        JmmNode rhs = node.getJmmChild(1);
        if ((!rhs.getKind().equals("IntLiteral") && !rhs.getKind().equals("BooleanLiteral")) || node.getChildren().size() > 2) {
            constantAssigns.remove(targetName);
            return false;
        } else {
            constantAssigns.put(targetName, rhs);
            scheduledAssignRemovals.put(targetName, node);
            return true;
        }
    }

    private void removeAllConstantAssignsAndVarDeclarations() {
        for (Map.Entry<String, JmmNode> assign : scheduledAssignRemovals.entrySet()){
            if (!constantAssigns.containsKey(assign.getKey())){
                continue;
            }
            assign.getValue().getJmmParent().removeJmmChild(assign.getValue());
        }

        for (Map.Entry<String, JmmNode> varDeclaration : varDeclarations.entrySet()) {
            if (!constantAssigns.containsKey(varDeclaration.getKey())){
                continue;
            }
            varDeclaration.getValue().getJmmParent().removeJmmChild(varDeclaration.getValue());
        }
    }
}
