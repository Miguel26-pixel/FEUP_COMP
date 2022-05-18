package pt.up.fe.comp;
/**
 * Copyright 2021 SPeCS.
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.specs.comp.ollir.parser.OllirParser;
import pt.up.fe.comp.backend.JmmBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.ollir.OllirUtils;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

import java.util.HashMap;
import java.util.Map;

public class BackendTest {

    // @Test
    // public void testHelloWorld() {
    // var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
    // TestUtils.noErrors(result.getReports());
    // var output = result.run();
    // assertEquals("Hello, World!", output.trim());
    // }

    @Test
    public void testHelloWorld() {
        String jasminCode = SpecsIo.getResource("fixtures/public/jasmin/HelloWorld.j");
        var output = TestUtils.runJasmin(jasminCode);
        assertEquals("Hello World!\nHello World Again!\n", SpecsStrings.normalizeFileContents(output));
    }

    @Test
    public void testToJasmin() {
        String ollirCode = SpecsIo.getResource("fixtures/public/cp2/OllirToJasminFields.ollir");

        Map<String, String> config = new HashMap<>();
        OllirResult ollirResult = new OllirResult(ollirCode, config);

        System.out.println(new JmmBackend().toJasmin(ollirResult).getJasminCode());
    }
}
