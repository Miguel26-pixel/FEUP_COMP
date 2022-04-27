package pt.up.fe.comp.semantic.visitors;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.List;

public class ImportDeclarationVisitor extends PreorderJmmVisitor<List<String>, Boolean> {

    public ImportDeclarationVisitor() {
        addVisit("ImportDeclaration", this::visitImportDeclaration);
    }

    private Boolean visitImportDeclaration(JmmNode importDeclaration, List<String> imports) {
        return true;
    }
}
