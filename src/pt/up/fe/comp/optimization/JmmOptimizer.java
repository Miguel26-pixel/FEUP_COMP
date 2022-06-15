package pt.up.fe.comp.optimization;

import org.specs.comp.ollir.ClassUnit;
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
        if (ollirResult.getConfig().containsKey("registerAllocation")
                && (Integer.parseInt(ollirResult.getConfig().get("registerAllocation")) >= 0)) {
            int n = Integer.parseInt(ollirResult.getConfig().get("registerAllocation"));
            ClassUnit classUnit = ollirResult.getOllirClass();
            RegAlloc optimizer = new RegAlloc(classUnit, ollirResult.getReports());
            optimizer.allocateRegs(n);
        }
        return ollirResult;
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        if (semanticsResult.getConfig().containsKey("optimize")
                && semanticsResult.getConfig().get("optimize").equals("true")) {
            ConstantPropagationVisitor constantPropagationVisitor =
                    new ConstantPropagationVisitor((JmmSymbolTable) semanticsResult.getSymbolTable());
            constantPropagationVisitor.visit(semanticsResult.getRootNode());
        }
        return semanticsResult;
    }
}
