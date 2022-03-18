package pt.up.fe.comp.jmm.parser;

import java.util.Map;

/**
 * Parses J-- code.
 * 
 * @author COMP2021
 *
 */
public interface JmmParser {

    JmmParserResult parse(String jmmCode, Map<String, String> config);

}