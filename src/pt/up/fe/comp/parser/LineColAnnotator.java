package pt.up.fe.comp.parser;

import pt.up.fe.comp.BaseNode;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class LineColAnnotator extends PreorderJmmVisitor<Boolean, Boolean> {
    public LineColAnnotator() {
        setDefaultVisit(this::annotateLineCol);
    }

    private Boolean annotateLineCol(JmmNode node, Boolean dummy) {
        BaseNode baseNode = (BaseNode) node;
        node.put("line", Integer.toString(baseNode.getBeginLine()));
        node.put("col", Integer.toString(baseNode.getBeginColumn()));
        return true;
    }
}