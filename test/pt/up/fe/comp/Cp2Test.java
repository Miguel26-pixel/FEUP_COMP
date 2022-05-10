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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collections;

import org.junit.Test;

import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

public class Cp2Test {

    public static void testOllirToJasmin(String resource, String expectedOutput) {
        // If AstToJasmin pipeline, do not execute test
        if (TestUtils.hasAstToJasminClass()) {
            return;
        }

        var ollirResult = new OllirResult(SpecsIo.getResource(resource), Collections.emptyMap());

        var result = TestUtils.backend(ollirResult);

        var testName = new File(resource).getName();
        System.out.println(testName + ":\n" + result.getJasminCode());
        var runOutput = result.runWithFullOutput();
        assertEquals("Error while running compiled Jasmin: " + runOutput.getOutput(), 0, runOutput.getReturnValue());
        System.out.println("\n Result: " + runOutput.getOutput());

        if (expectedOutput != null) {
            assertEquals(expectedOutput, runOutput.getOutput());
        }
    }

    public static void testOllirToJasmin(String resource) {
        testOllirToJasmin(resource, null);
    }

    public static void testJmmCompilation(String resource, String expectedOutput) {
        // If AstToJasmin pipeline, generate Jasmin
        if (TestUtils.hasAstToJasminClass()) {

            var result = TestUtils.backend(SpecsIo.getResource(resource));

            var testName = new File(resource).getName();
            System.out.println(testName + ":\n" + result.getJasminCode());
            var runOutput = result.runWithFullOutput();
            assertEquals("Error while running compiled Jasmin: " + runOutput.getOutput(), 0,
                    runOutput.getReturnValue());
            System.out.println("\n Result: " + runOutput.getOutput());

            if (expectedOutput != null) {
                assertEquals(expectedOutput, runOutput.getOutput());
            }

            return;
        }

        var result = TestUtils.optimize(SpecsIo.getResource(resource));
        var testName = new File(resource).getName();
        System.out.println(testName + ":\n" + result.getOllirCode());
    }

    public static void testJmmCompilation(String resource) {
        testJmmCompilation(resource, null);
    }

    @Test
    public void test_1_00_SymbolTable() {
        // System.out.println(TestUtils.parse(SpecsIo.getResource("fixtures/public/cp2/SymbolTable.jmm"))
        // .getRootNode().toTree());

        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/SymbolTable.jmm"));
        System.out.println("Symbol Table:\n" + result.getSymbolTable().print());
    }

    @Test
    public void test_1_01_VarNotDeclared() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/VarNotDeclared.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_02_ClassNotImported() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/ClassNotImported.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_03_IntPlusObject() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/IntPlusObject.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_04_BoolTimesInt() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/BoolTimesInt.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_05_ArrayPlusInt() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/ArrayPlusInt.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_06_ArrayAccessOnInt() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/ArrayAccessOnInt.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_07_ArrayIndexNotInt() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/ArrayIndexNotInt.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_08_AssignIntToBool() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/AssignIntToBool.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_09_ObjectAssignmentFail() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/ObjectAssignmentFail.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_10_ObjectAssignmentPassExtends() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/ObjectAssignmentPassExtends.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void test_1_11_ObjectAssignmentPassImports() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/ObjectAssignmentPassImports.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void test_1_12_IntInIfCondition() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/IntInIfCondition.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_13_ArrayInWhileCondition() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/ArrayInWhileCondition.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_14_CallToUndeclaredMethod() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/CallToUndeclaredMethod.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_15_CallToMethodAssumedInExtends() {
        var result = TestUtils
                .analyse(SpecsIo.getResource("fixtures/public/cp2/CallToMethodAssumedInExtends.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void test_1_16_CallToMethodAssumedInImport() {
        var result = TestUtils
                .analyse(SpecsIo.getResource("fixtures/public/cp2/CallToMethodAssumedInImport.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void test_1_17_IncompatibleArguments() {
        var result = TestUtils
                .analyse(SpecsIo.getResource("fixtures/public/cp2/IncompatibleArguments.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_18_IncompatibleReturn() {
        var result = TestUtils
                .analyse(SpecsIo.getResource("fixtures/public/cp2/IncompatibleReturn.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void test_1_19_AssumeArguments() {
        var result = TestUtils
                .analyse(SpecsIo.getResource("fixtures/public/cp2/AssumeArguments.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void test_2_01_CompileBasic() {
        testJmmCompilation("fixtures/public/cp2/CompileBasic.jmm");
    }

    @Test
    public void test_2_02_CompileArithmetic() {
        testJmmCompilation("fixtures/public/cp2/CompileArithmetic.jmm");
    }

    @Test
    public void test_2_03_CompileMethodInvocation() {
        testJmmCompilation("fixtures/public/cp2/CompileMethodInvocation.jmm");
    }

    @Test
    public void test_2_04_CompileAssignment() {
        testJmmCompilation("fixtures/public/cp2/CompileAssignment.jmm");
    }

    @Test
    public void test_3_01_OllirToJasminBasic() {
        testOllirToJasmin("fixtures/public/cp2/OllirToJasminBasic.ollir");
    }

    @Test
    public void test_3_02_OllirToJasminArithmetics() {
        testOllirToJasmin("fixtures/public/cp2/OllirToJasminArithmetics.ollir");
    }

    @Test
    public void test_3_03_OllirToJasminInvoke() {
        testOllirToJasmin("fixtures/public/cp2/OllirToJasminInvoke.ollir");
    }

    @Test
    public void test_3_04_OllirToJasminFields() {
        testOllirToJasmin("fixtures/public/cp2/OllirToJasminFields.ollir");
    }

}
