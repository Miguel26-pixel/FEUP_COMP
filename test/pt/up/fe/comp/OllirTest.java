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

import org.junit.Test;
import pt.up.fe.specs.util.SpecsIo;
import java.util.HashMap;
import java.util.Map;
public class OllirTest {
    private void testOptimize(String resourcePath) {
        Map<String, String> config =  new HashMap<>();
        config.put("optimize", "true");
        var result = TestUtils.optimize(SpecsIo.getResource(resourcePath), config);
        System.out.println(result.getOllirCode());
        var unoptimized = TestUtils.optimize(SpecsIo.getResource(resourcePath));
        System.out.println("wowo");
        System.out.println(unoptimized.getOllirCode());
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void binOps() {
        testOptimize("fixtures/public/BinOps.jmm");
    }

    @Test
    public void helloWorld() {
        testOptimize("fixtures/public/HelloWorld.jmm");
    }

    @Test
    public void fac() {
        testOptimize("fixtures/public/Fac.jmm");
    }

    @Test
    public void findMaximum() {
        testOptimize("fixtures/public/FindMaximum.jmm");
    }

    @Test
    public void quickestSort() {
        testOptimize("fixtures/public/QuickestSort.jmm");
    }

    @Test
    public void lazySort() {
        testOptimize("fixtures/public/LazySort.jmm");
    }

    @Test
    public void life() {
        testOptimize("fixtures/public/Life.jmm");
    }

    @Test
    public void quickSort() {
        testOptimize("fixtures/public/QuickSort.jmm");
    }

    @Test
    public void simple() {
        testOptimize("fixtures/public/Simple.jmm");
    }

    @Test
    public void ticTacToe() {
        testOptimize("fixtures/public/TicTacToe.jmm");
    }

    @Test
    public void whileAndIf() {
        testOptimize("fixtures/public/WhileAndIf.jmm");
    }

    @Test
    public void varLookupField() {
        testOptimize("fixtures/public/cpf/2_semantic_analysis/lookup/VarLookup_Field.jmm");
    }

    @Test
    public void setInline() {
        testOptimize("fixtures/public/cpf/4_jasmin/calls/PrintOtherClassInline.jmm");
    }
}
