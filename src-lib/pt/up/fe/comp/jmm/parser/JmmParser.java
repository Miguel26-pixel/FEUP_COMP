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

    default JmmParserResult parse(String jmmCode, String startingRule, Map<String, String> config) {
        return parse(jmmCode, config);
    }

}