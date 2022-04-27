package pt.up.fe.comp.parser;

import java.util.Collections;
import java.util.Map;

import pt.up.fe.comp.*;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParser;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;

/**
 * Copyright 2022 SPeCS.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

public class SimpleParser implements JmmParser {

    @Override
    public JmmParserResult parse(String jmmCode, Map<String, String> config) {
        return this.parse(jmmCode, "Start", config);
    }

    @Override
    public JmmParserResult parse(String jmmCode, String startingRule, Map<String, String> config) {
        try {
            JmmGrammarParser parser = new JmmGrammarParser(SpecsIo.toInputStream(jmmCode));
            SpecsSystem.invoke(parser, startingRule);
            Node root = parser.rootNode();
            if (!(root instanceof JmmNode)) {
                return JmmParserResult.newError(new Report(ReportType.WARNING, Stage.SYNTATIC, -1,
                        "JmmNode interface not yet implemented, returning null root node"));
            }
            JmmParserResult result = new JmmParserResult((JmmNode) root, Collections.emptyList(), config);
            System.out.println(result.getRootNode().toTree());
            return result;
        } catch (Exception e) {
            var exception = TestUtils.getException(e, ParseException.class);
            assert exception != null;
            Token t = exception.getToken();
            int line = t.getBeginLine();
            int column = t.getBeginColumn();
            String message = exception.getMessage();
            Report report = Report.newError(Stage.SYNTATIC, line, column, message, exception);
            return JmmParserResult.newError(report);
        }
    }
}
