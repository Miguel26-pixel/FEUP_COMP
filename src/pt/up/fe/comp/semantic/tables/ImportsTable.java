package pt.up.fe.comp.semantic.tables;

import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.semantic.visitors.ImportDeclarationVisitor;

import java.util.ArrayList;
import java.util.List;

public class ImportsTable {
    private final List<String> imports;

    public ImportsTable(JmmParserResult parserResult) {
        this.imports = new ArrayList<>();

        ImportDeclarationVisitor idv = new ImportDeclarationVisitor();
        idv.visit(parserResult.getRootNode(), this.imports);
    }

    public List<String> getImports() {
        return imports;
    }

    public Boolean isImported(String importName) {
        return imports.contains(importName);
    }
}
