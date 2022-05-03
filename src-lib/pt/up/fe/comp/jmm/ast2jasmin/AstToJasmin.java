package pt.up.fe.comp.jmm.ast2jasmin;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;

/**
 * Special stage for groups that will not be doing OLLIR optimizations (e.g. due to being a grupo of 2 elements). <br>
 * 
 * This Stage deals with optimizations performed at the AST level and conversion from AST directly to Jasmin
 * Bytecodes.<br>
 * <br>
 * 
 * Note that for Checkpoint 2 (CP2) only the {@link AstToJasmin#toJasmin(JmmSemanticsResult)} has to be developed. The
 * method {@link AstToJasmin#optimize(JmmSemanticsResult)} is for Checkpoint 3 (CP3).
 */
public interface AstToJasmin {

    /**
     * Step 1 (for CP3): optimize code at the AST level
     * 
     * @param semanticsResult
     * @return
     */
    default JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        return semanticsResult;
    }

    /**
     * 
     * Step 2 (for CP2): convert the AST to Jasmin Bytecodes.<br>
     * <br>
     * 
     * Note that this step for Checkpoint 2 (CP2) only includes the code structures defined in the project description.
     * 
     * @param semanticsResult
     * @return
     */
    JasminResult toJasmin(JmmSemanticsResult semanticsResult);

}