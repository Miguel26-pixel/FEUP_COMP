package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;
import pt.up.fe.comp.semantic.visitors.symbolTableLookup.ArrayAccessVisitor;
import pt.up.fe.comp.semantic.visitors.symbolTableLookup.MethodCallVisitor;
import pt.up.fe.comp.semantic.visitors.symbolTableLookup.OperandsVisitor;
import pt.up.fe.comp.semantic.visitors.symbolTableLookup.VariableAccessVisitor;

import java.util.*;

public class JmmAnalyser implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        JmmSymbolTable symbolTable = new JmmSymbolTable(parserResult);
        List<Report> reports = new ArrayList<>(symbolTable.getReports());

        // Check variable existence
        VariableAccessVisitor variableAccessVisitor = new VariableAccessVisitor(symbolTable);
        variableAccessVisitor.visit(parserResult.getRootNode(), Boolean.TRUE);
        reports.addAll(variableAccessVisitor.getReports());

        // Check called methods existence
        MethodCallVisitor methodCallVisitor = new MethodCallVisitor(symbolTable);
        methodCallVisitor.visit(parserResult.getRootNode(), Boolean.TRUE);
        reports.addAll(methodCallVisitor.getReports());

        // Check array access on array type
        ArrayAccessVisitor arrayAccessVisitor = new ArrayAccessVisitor(symbolTable);
        arrayAccessVisitor.visit(parserResult.getRootNode(), Boolean.TRUE);
        reports.addAll(arrayAccessVisitor.getReports());

        // Check operands type
        OperandsVisitor operandsVisitor = new OperandsVisitor(symbolTable);
        operandsVisitor.visit(parserResult.getRootNode(), Boolean.TRUE);
        reports.addAll(operandsVisitor.getReports());

        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
