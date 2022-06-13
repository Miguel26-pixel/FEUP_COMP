package pt.up.fe.comp.parser;

import pt.up.fe.comp.BaseNode;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.Arrays;

public class ReservedVariableNameChanger extends PreorderJmmVisitor<Boolean, Boolean> {
    public ReservedVariableNameChanger() {
        setDefaultVisit(this::changeReservedNames);
    }

    private boolean isReservedName(String name){
        final String[] reserved = {"ret", "putfield", "getstatic", "invokespecial", "invokestatic", "invokevirtual", "field"};
        return Arrays.asList(reserved).contains(name);
    }

    private Boolean changeReservedNames(JmmNode node, Boolean dummy) {
        if (!node.getAttributes().contains("name") || !isReservedName(node.get("name"))){
            return true;
        }
        node.put("name", node.get("name") + "_");
        return true;
    }
}