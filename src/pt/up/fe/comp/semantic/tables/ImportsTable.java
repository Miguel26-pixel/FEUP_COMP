package pt.up.fe.comp.semantic.tables;

import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.semantic.visitors.ImportDeclarationVisitor;

import java.util.ArrayList;
import java.util.List;

public class ImportsTable {
    private final List<String> imports = new ArrayList<>();

    public ImportsTable(JmmParserResult parserResult) {
        ImportDeclarationVisitor importDeclarationVisitor = new ImportDeclarationVisitor();
        importDeclarationVisitor.visit(parserResult.getRootNode(), this.imports);
    }

    public List<String> getImports() {
        return imports;
    }
}
