package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.semantic.visitors.ImportDeclarationVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JmmAnalyser implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        SymbolTable symbolTable = null;
        ImportDeclarationVisitor idv = new ImportDeclarationVisitor();
        List<String> imports = new ArrayList<>();
        
        idv.visit(parserResult.getRootNode(), imports);
        
        return new JmmSemanticsResult(parserResult, symbolTable, Collections.emptyList());
    }
}
