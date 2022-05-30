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

public class OptimizeTest {

    private void testOptimize(String resourcePath) {
        var result = TestUtils.optimize(SpecsIo.getResource(resourcePath));
        System.out.println(result.getOllirCode());
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testBinOps() {
        testOptimize("fixtures/public/BinOps.jmm");
    }

    @Test
    public void testHelloWorld() {
        testOptimize("fixtures/public/HelloWorld.jmm");
    }

    @Test
    public void testFac() {
        testOptimize("fixtures/public/Fac.jmm");
    }

    @Test
    public void testFindMaximum() {
        testOptimize("fixtures/public/FindMaximum.jmm");
    }

    @Test
    public void testQuickestSort() {
        testOptimize("fixtures/public/QuickestSort.jmm");
    }
}
