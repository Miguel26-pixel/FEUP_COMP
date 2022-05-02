package pt.up.fe.comp.semantic.visitors;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.stream.Collectors;

public class ImportDeclarationVisitor extends ReportCollectorJmmNodeVisitor<List<String>, Boolean> {

    public ImportDeclarationVisitor() {
        addVisit("Start", this::visitStart);
        addVisit("ImportDeclaration", this::visitImportDeclaration);

        setDefaultVisit((node, imports) -> true);
    }

    private Boolean visitStart(JmmNode start, List<String> imports) {
        for (JmmNode child : start.getChildren()) {
            visit(child, imports);
        }
        return true;
    }

    private Boolean visitImportDeclaration(JmmNode importDeclaration, List<String> imports) {
        var importString = importDeclaration.getChildren().stream()
                .map(id -> id.get("name"))
                .collect(Collectors.joining("."));

        imports.add(importString);

        return true;
    }
}
