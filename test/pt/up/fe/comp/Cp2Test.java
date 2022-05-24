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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Consumer;

import org.junit.Test;
import org.specs.comp.ollir.AssignInstruction;
import org.specs.comp.ollir.BinaryOpInstruction;
import org.specs.comp.ollir.CallInstruction;
import org.specs.comp.ollir.CallType;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.ReturnInstruction;

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

    public static void testJmmCompilation(String resource, Consumer<ClassUnit> ollirTester, String executionOutput) {

        // If AstToJasmin pipeline, generate Jasmin
        if (TestUtils.hasAstToJasminClass()) {

            var result = TestUtils.backend(SpecsIo.getResource(resource));

            var testName = new File(resource).getName();
            System.out.println(testName + ":\n" + result.getJasminCode());
            var runOutput = result.runWithFullOutput();
            assertEquals("Error while running compiled Jasmin: " + runOutput.getOutput(), 0,
                    runOutput.getReturnValue());
            System.out.println("\n Result: " + runOutput.getOutput());

            if (executionOutput != null) {
                assertEquals(executionOutput, runOutput.getOutput());
            }

            return;
        }

        var result = TestUtils.optimize(SpecsIo.getResource(resource));
        var testName = new File(resource).getName();
        System.out.println(testName + ":\n" + result.getOllirCode());

        if (ollirTester != null) {
            ollirTester.accept(result.getOllirClass());
        }
    }

    public static void testJmmCompilation(String resource) {
        testJmmCompilation(resource, null);
    }

    public static void testJmmCompilation(String resource, Consumer<ClassUnit> ollirTester) {
        testJmmCompilation(resource, ollirTester, null);
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
        testJmmCompilation("fixtures/public/cp2/CompileBasic.jmm", this::ollirTest_2_01_CompileBasic);
    }

    @Test
    public void test_2_02_CompileArithmetic() {
        testJmmCompilation("fixtures/public/cp2/CompileArithmetic.jmm", this::ollirTest_2_02_CompileArithmetic);
    }

    @Test
    public void test_2_03_CompileMethodInvocation() {
        testJmmCompilation("fixtures/public/cp2/CompileMethodInvocation.jmm",
                this::ollirTest_2_03_CompileMethodInvocation);
    }

    @Test
    public void test_2_04_CompileAssignment() {
        testJmmCompilation("fixtures/public/cp2/CompileAssignment.jmm", this::ollirTest_2_04_CompileAssignment);
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

    public void ollirTest_2_01_CompileBasic(ClassUnit classUnit) {
        // Test name of the class and super
        assertEquals("Class name not what was expected", "SymbolTable", classUnit.getClassName());
        assertEquals("Super class name not what was expected", "Quicksort", classUnit.getSuperClass());

        // Test fields
        assertEquals("Class should have two fields", 2, classUnit.getNumFields());
        var fieldNames = new HashSet<>(Arrays.asList("intField", "boolField"));
        assertThat(fieldNames, hasItem(classUnit.getField(0).getFieldName()));
        assertThat(fieldNames, hasItem(classUnit.getField(1).getFieldName()));

        // Test method 1
        Method method1 = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals("method1"))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method1", method1);

        var retInst1 = method1.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method1", retInst1.isPresent());

        // Test method 2
        Method method2 = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals("method2"))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method2'", method2);

        var retInst2 = method2.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method2", retInst2.isPresent());
    }

    public void ollirTest_2_02_CompileArithmetic(ClassUnit classUnit) {
        // Test name of the class
        assertEquals("Class name not what was expected", "Test", classUnit.getClassName());

        // Test foo
        var methodName = "foo";
        Method methodFoo = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals(methodName))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method " + methodName, methodFoo);

        var binOpInst = methodFoo.getInstructions().stream()
                .filter(inst -> inst instanceof AssignInstruction)
                .map(instr -> (AssignInstruction) instr)
                .filter(assign -> assign.getRhs() instanceof BinaryOpInstruction)
                .findFirst();

        assertTrue("Could not find a binary op instruction in method " + methodName, binOpInst.isPresent());

        var retInst = methodFoo.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method " + methodName, retInst.isPresent());

    }

    public void ollirTest_2_03_CompileMethodInvocation(ClassUnit classUnit) {
        // Test name of the class
        assertEquals("Class name not what was expected", "Test", classUnit.getClassName());

        // Test foo
        var methodName = "foo";
        Method methodFoo = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals(methodName))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method " + methodName, methodFoo);

        var callInst = methodFoo.getInstructions().stream()
                .filter(inst -> inst instanceof CallInstruction)
                .map(CallInstruction.class::cast)
                .findFirst();
        assertTrue("Could not find a call instruction in method " + methodName, callInst.isPresent());

        assertEquals("Invocation type not what was expected", CallType.invokestatic,
                callInst.get().getInvocationType());
    }

    public void ollirTest_2_04_CompileAssignment(ClassUnit classUnit) {
        // Test name of the class
        assertEquals("Class name not what was expected", "Test", classUnit.getClassName());

        // Test foo
        var methodName = "foo";
        Method methodFoo = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals(methodName))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method " + methodName, methodFoo);

        var assignInst = methodFoo.getInstructions().stream()
                .filter(inst -> inst instanceof AssignInstruction)
                .map(AssignInstruction.class::cast)
                .findFirst();
        assertTrue("Could not find an assign instruction in method " + methodName, assignInst.isPresent());

        assertEquals("Assignment does not have the expected type", ElementType.INT32,
                assignInst.get().getTypeOfAssign().getTypeOfElement());
    }

}
