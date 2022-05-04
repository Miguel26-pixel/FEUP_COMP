package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;
import pt.up.fe.comp.semantic.visitors.ArrayAccessVisitor;

import java.util.*;

public class JmmAnalyser implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        JmmSymbolTable symbolTable = new JmmSymbolTable(parserResult);
        List<Report> reports = new ArrayList<>(symbolTable.getReports());

        // Array access analyzer
        ArrayAccessVisitor arrayAccessVisitor = new ArrayAccessVisitor(symbolTable);
        arrayAccessVisitor.visit(parserResult.getRootNode(), Boolean.TRUE);
        reports.addAll(arrayAccessVisitor.getReports());

        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
