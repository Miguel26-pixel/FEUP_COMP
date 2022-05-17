package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;

import java.util.Collections;

public class JmmOptimizer implements JmmOptimization {

    StringBuilder ollirCode = new StringBuilder();

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        OllirEmitter ollirEmitter = new OllirEmitter(ollirCode, (JmmSymbolTable) semanticsResult.getSymbolTable(), 4);
        ollirEmitter.visit(semanticsResult.getRootNode());
        return new OllirResult(semanticsResult, ollirCode.toString(), Collections.emptyList());
    }

}
