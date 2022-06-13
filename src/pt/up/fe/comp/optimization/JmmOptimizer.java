package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;

import java.util.Collections;

public class JmmOptimizer implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        final StringBuilder ollirCode = new StringBuilder();
        OllirEmitter ollirEmitter = new OllirEmitter(ollirCode, (JmmSymbolTable) semanticsResult.getSymbolTable(), 4);
        ollirEmitter.visit(semanticsResult.getRootNode());
        return new OllirResult(semanticsResult, ollirCode.toString(), Collections.emptyList());
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        return ollirResult;
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        if (semanticsResult.getConfig().containsKey("optimize")
                && semanticsResult.getConfig().get("optimize").equals("true")) {
            new ConstantPropagationVisitor().visit(semanticsResult.getRootNode());
        }
        return semanticsResult;
    }

}
