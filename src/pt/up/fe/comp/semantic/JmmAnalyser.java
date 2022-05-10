package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;
import pt.up.fe.comp.semantic.visitors.symbolTableLookup.ArrayAccessVisitor;
import pt.up.fe.comp.semantic.visitors.symbolTableLookup.ImportCheckVisitor;
import pt.up.fe.comp.semantic.visitors.symbolTableLookup.MethodCallVisitor;
import pt.up.fe.comp.semantic.visitors.symbolTableLookup.TypeCheckVisitor;

import java.util.*;

public class JmmAnalyser implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        JmmSymbolTable symbolTable = new JmmSymbolTable(parserResult);
        List<Report> reports = new ArrayList<>(symbolTable.getReports());

        // Check array access on array type
        ArrayAccessVisitor arrayAccessVisitor = new ArrayAccessVisitor(symbolTable);
        arrayAccessVisitor.visit(parserResult.getRootNode(), Boolean.TRUE);
        reports.addAll(arrayAccessVisitor.getReports());

        // Type verification
        TypeCheckVisitor typeCheckVisitor = new TypeCheckVisitor(symbolTable);
        typeCheckVisitor.visit(parserResult.getRootNode(), new Type("",false));
        reports.addAll(typeCheckVisitor.getReports());

        // Check called methods existence
        MethodCallVisitor methodCallVisitor = new MethodCallVisitor(symbolTable);
        methodCallVisitor.visit(parserResult.getRootNode(), Boolean.TRUE);
        reports.addAll(methodCallVisitor.getReports());

        // Check extern classes and imports
        ImportCheckVisitor importCheckVisitor = new ImportCheckVisitor(symbolTable);
        importCheckVisitor.visit(parserResult.getRootNode(), new Type("",false));
        reports.addAll(importCheckVisitor.getReports());

        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
