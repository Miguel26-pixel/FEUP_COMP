/**
 * Copyright 2022 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.comp;

import org.junit.Test;

import pt.up.fe.specs.util.SpecsIo;

public class ParserTest {

    private static void noErrors(String code) {
        var result = TestUtils.parse(code);
        TestUtils.noErrors(result);
    }

    private static void mustFail(String code) {
        var result = TestUtils.parse(code);
        TestUtils.mustFail(result);
    }

    /* 
     * Code that must be successfully parsed 
     */

    @Test
    public void helloWorld() {
        noErrors(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
    }

    @Test
    public void findMaximum() {
        noErrors(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
    }

    @Test
    public void lazysort() {
        noErrors(SpecsIo.getResource("fixtures/public/Lazysort.jmm"));
    }

    @Test
    public void life() {
        noErrors(SpecsIo.getResource("fixtures/public/Life.jmm"));
    }

    @Test
    public void quickSort() {
        noErrors(SpecsIo.getResource("fixtures/public/QuickSort.jmm"));
    }

    @Test
    public void simple() {
        noErrors(SpecsIo.getResource("fixtures/public/Simple.jmm"));
    }

    @Test
    public void ticTacToe() {
        noErrors(SpecsIo.getResource("fixtures/public/TicTacToe.jmm"));
    }

    @Test
    public void whileAndIf() {
        noErrors(SpecsIo.getResource("fixtures/public/WhileAndIf.jmm"));
    }

    /* 
     * Code with errors
     */

    @Test
    public void blowUp() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/syntactical/BlowUp.jmm"));
    }

    @Test
    public void completeWhileTest() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/syntactical/CompleteWhileTest.jmm"));
    }

    @Test
    public void lengthError() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/syntactical/LengthError.jmm"));
    }

    @Test
    public void missingRightPar() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/syntactical/MissingRightPar.jmm"));
    }

    @Test
    public void multipleSequential() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/syntactical/MultipleSequential.jmm"));
    }

    @Test
    public void nestedLoop() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/syntactical/NestedLoop.jmm"));
    }
}
