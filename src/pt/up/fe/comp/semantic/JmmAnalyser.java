package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.semantic.visitors.ImportDeclarationVisitor;
import pt.up.fe.comp.semantic.visitors.MethodDeclarationVisitor;

import java.util.*;

public class JmmAnalyser implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        SymbolTable symbolTable = new JmmSymbolTable(parserResult);

        return new JmmSemanticsResult(parserResult, symbolTable, Collections.emptyList());
    }
}
