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

import java.util.Collections;

import org.junit.Test;

import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

public class Cp2Test {

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
    public void test_2_01_OllirBasic() {
        var result = TestUtils
                .optimize(SpecsIo.getResource("fixtures/public/cp2/OllirBasic.jmm"));

        System.out.println("OllirBasic:\n" + result.getOllirCode());
    }

    @Test
    public void test_2_02_OllirArithmetic() {
        var result = TestUtils
                .optimize(SpecsIo.getResource("fixtures/public/cp2/OllirArithmetic.jmm"));

        System.out.println("OllirArithmetic:\n" + result.getOllirCode());
    }

    @Test
    public void test_2_03_OllirMethodInvocation() {
        var result = TestUtils
                .optimize(SpecsIo.getResource("fixtures/public/cp2/OllirMethodInvocation.jmm"));

        System.out.println("OllirMethodInvocation:\n" + result.getOllirCode());
    }

    @Test
    public void test_2_04_OllirAssignment() {
        var result = TestUtils
                .optimize(SpecsIo.getResource("fixtures/public/cp2/OllirAssignment.jmm"));

        System.out.println("OllirAssignment:\n" + result.getOllirCode());
    }

    @Test
    public void test_3_01_OllirToJasminBasic() {
        var ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/cp2/OllirToJasminBasic.ollir"),
                Collections.emptyMap());

        var result = TestUtils.backend(ollirResult);

        System.out.println("OllirToJasminBasic:\n" + result.getJasminCode());
        result.compile();
        System.out.println("\n Result: " + result.run());
    }

    @Test
    public void test_3_02_OllirToJasminArithmetics() {
        var ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/cp2/OllirToJasminArithmetics.ollir"),
                Collections.emptyMap());

        var result = TestUtils.backend(ollirResult);

        System.out.println("OllirToJasminArithmetics:\n" + result.getJasminCode());
        result.compile();
        System.out.println("\n Result: " + result.run());
    }

    @Test
    public void test_3_03_OllirToJasminInvoke() {
        var ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/cp2/OllirToJasminInvoke.ollir"),
                Collections.emptyMap());

        var result = TestUtils.backend(ollirResult);

        System.out.println("OllirToJasminInvoke:\n" + result.getJasminCode());
        result.compile();
        System.out.println("\n Result: " + result.run());
    }

    @Test
    public void test_3_04_OllirToJasminFields() {
        var ollirResult = new OllirResult(SpecsIo.getResource("fixtures/public/cp2/OllirToJasminFields.ollir"),
                Collections.emptyMap());

        var result = TestUtils.backend(ollirResult);

        System.out.println("OllirToJasminFields:\n" + result.getJasminCode());
        result.compile();
        System.out.println("\n Result: " + result.run());
    }

}
