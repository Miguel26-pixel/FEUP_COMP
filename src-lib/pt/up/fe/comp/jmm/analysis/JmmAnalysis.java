package pt.up.fe.comp.jmm.analysis;

import pt.up.fe.comp.jmm.parser.JmmParserResult;

/**
 * This stage deals with analysis performed at the AST level, essentially semantic analysis and symbol table generation.
 */
public interface JmmAnalysis {

    JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult);

    // TODO: Activate next year
    // Test for this (that enabling/disabling works)
    // JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult, boolean enableAnalysis);

}