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

package pt.up.fe.comp.cpf;

import static org.hamcrest.CoreMatchers.hasItem;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.junit.Test;
import org.specs.comp.ollir.ArrayOperand;
import org.specs.comp.ollir.AssignInstruction;
import org.specs.comp.ollir.CallInstruction;
import org.specs.comp.ollir.CallType;
import org.specs.comp.ollir.ClassType;
import org.specs.comp.ollir.CondBranchInstruction;
import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.GotoInstruction;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.OperationType;

import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

public class Cpf3_Ollir {

    static OllirResult getOllirResult(String filename) {
        return TestUtils.optimize(SpecsIo.getResource("fixtures/public/cpf/3_ollir/" + filename));
    }

    /**
     * Test the declaration of the main class
     */
    @Test
    public void section1_Basic_Structure_main() {
        var result = getOllirResult("basic/Structure_class.jmm");

        var method = CpUtils.getMethod(result, "main");

        CpUtils.assertEquals("Is name of the method name?", "main", method.getMethodName(), result);
        CpUtils.assertEquals("Is return type void?", "void", CpUtils.toString(method.getReturnType()), result);
        CpUtils.assertEquals("Is first parameter String[]?", "String[]", CpUtils.toString(method.getParam(0).getType()),
                result);
        CpUtils.assertEquals("Has public access modifier?", "PUBLIC", method.getMethodAccessModifier().toString(),
                result);
        CpUtils.assertTrue("Is static method?", method.isStaticMethod(), result);
    }

    /**
     * Test the declaration of class fields of type int[]
     */
    @Test
    public void section1_Basic_Structure_fields_int_arr() {
        var result = getOllirResult("basic/Structure_fields_intarray.jmm");

        var classUnit = result.getOllirClass();

        CpUtils.assertEquals("Number of fields", 1, classUnit.getNumFields(), result);
        CpUtils.assertEquals("Type of field", "int[]", CpUtils.toString(classUnit.getField(0).getFieldType()), result);
    }

    @Test
    public void section1_Basic_BasicImports_Package() {
        var result = getOllirResult("basic/BasicImportsPackages.jmm");
        var classUnit = result.getOllirClass();

        var imports = classUnit.getImports();

        CpUtils.assertEquals("Number of imports", 2, imports.size(), result);

        var importNames = new HashSet<>(imports);
        var expectedNames = Arrays.asList("foo.bar", "pt.up.fe.comp.io");

        importNames.stream().forEach(importName -> CpUtils.assertThat("Expected imports", expectedNames,
                hasItem(importName), result));
    }

    /*checks if method declaration is correct (class)*/
    @Test
    public void section1_Basic_Method_Declaration_Class() {
        var result = getOllirResult("basic/BasicMethodsClass.jmm");

        var method = CpUtils.getMethod(result, "func3");

        CpUtils.assertEquals("Method return type", "BasicMethods", CpUtils.toString(method.getReturnType()), result);
    }

    /*checks if method declaration is correct (array)*/
    @Test
    public void section1_Basic_Method_Declaration_Array() {
        var result = getOllirResult("basic/BasicMethodsArray.jmm");

        var method = CpUtils.getMethod(result, "func4");

        CpUtils.assertEquals("Method return type", "int[]", CpUtils.toString(method.getReturnType()), result);
    }

    /**
     * Test the code of an integer division: Variables are loaded with iload idiv is called The result is stored using
     * istore
     */
    @Test
    public void section2_Arithmetic_Simple_div() {
        var ollirResult = getOllirResult("arithmetic/Arithmetic_div.jmm");

        var method = CpUtils.getMethod(ollirResult, "main");

        CpUtils.assertHasOperation(OperationType.DIV, method, ollirResult);
    }

    @Test
    public void section2_Arithmetic_Simple_and() {
        var ollirResult = getOllirResult("arithmetic/Arithmetic_and.jmm");

        var method = CpUtils.getMethod(ollirResult, "main");

        CpUtils.assertHasOperation(OperationType.ANDB, method, ollirResult);
    }

    @Test
    public void section2_Arithmetic_Simple_less() {
        var ollirResult = getOllirResult("arithmetic/Arithmetic_less.jmm");

        var method = CpUtils.getMethod(ollirResult, "main");

        CpUtils.assertHasOperation(OperationType.LTH, method, ollirResult);

    }

    @Test
    public void section3_ControlFlow_If_Simple_Single_goto() {

        var result = getOllirResult("control_flow/SimpleIfElseStat.jmm");

        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 1, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 1 goto", gotos.size() >= 1, result);
    }

    @Test
    public void section3_ControlFlow_If_Switch() {

        var result = getOllirResult("control_flow/SwitchStat.jmm");

        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 6, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 6 gotos", gotos.size() >= 6, result);
    }

    @Test
    public void section3_ControlFlow_While_Simple() {

        var result = getOllirResult("control_flow/SimpleWhileStat.jmm");

        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);

        CpUtils.assertTrue("Number of branches between 1 and 2", branches.size() > 0 && branches.size() < 3, result);
    }

    /*checks if dynamic invocations work properly*/
    @Test
    public void section4_Calls_Invoke_Virtual() {
        var result = getOllirResult("calls/NoArgsFuncCall.jmm");

        var method = CpUtils.getMethod(result, "bar");

        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);

        CpUtils.assertEquals("Number of calls", 1, calls.size(), result);

        CpUtils.assertEquals("Invocation type of call", CallType.invokevirtual, calls.get(0).getInvocationType(),
                result);
    }

    /*checks if static invocations work properly*/
    @Test
    public void section4_Calls_Invoke_Static() {
        var result = getOllirResult("calls/InvokeStatic.jmm");

        var method = CpUtils.getMethod(result, "func");

        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);

        CpUtils.assertEquals("Number of calls", 1, calls.size(), result);

        CpUtils.assertEquals("Invocation type of call", CallType.invokestatic, calls.get(0).getInvocationType(),
                result);
    }

    @Test
    public void section4_Calls_SetInlineAndPrintOtherClassFromParam() {

        var result = getOllirResult("calls/PrintOtherClassFromParam.jmm");

        // Test main

        var mainMethod = CpUtils.getMethod(result, "main");

        var mainCalls = CpUtils.assertInstExists(CallInstruction.class, mainMethod, result);

        CpUtils.assertTrue("Number of calls between 4 and 6", mainCalls.size() >= 4 && mainCalls.size() <= 6, result);

        // Get virtual calls
        var mainVirtualCalls = mainCalls.stream().filter(call -> call.getInvocationType() == CallType.invokevirtual)
                .collect(Collectors.toList());

        CpUtils.assertEquals("Number of virtual calls", 2, mainVirtualCalls.size(), result);

        var validMethodNames = Arrays.asList("GetterAndSetter", "PrintOtherClassFromParam");

        for (var call : mainVirtualCalls) {
            var base = call.getFirstArg();
            CpUtils.assertTrue("First arg of call is a variable (Operand)", base instanceof Operand, result);

            var operand = (Operand) base;
            var type = operand.getType();

            CpUtils.assertEquals("Type of first arg of call", ElementType.OBJECTREF, type.getTypeOfElement(), result);
            CpUtils.assertThat("Name of type of first arg of call", validMethodNames,
                    hasItem(((ClassType) type).getName()), result);
        }

        // Test print
        var printMethod = CpUtils.getMethod(result, "print");

        var printCalls = CpUtils.assertInstExists(CallInstruction.class, printMethod, result);

        CpUtils.assertEquals("Number of calls", 2, printCalls.size(), result);

        // Get virtual calls
        var printVirtualCalls = printCalls.stream().filter(call -> call.getInvocationType() == CallType.invokevirtual)
                .collect(Collectors.toList());

        CpUtils.assertEquals("Number of virtual calls", 1, printVirtualCalls.size(), result);

        for (var call : printVirtualCalls) {
            var base = call.getFirstArg();
            CpUtils.assertTrue("First arg of call is a variable (Operand)", base instanceof Operand, result);

            var operand = (Operand) base;
            var type = operand.getType();

            CpUtils.assertEquals("Type of first arg of call", ElementType.OBJECTREF, type.getTypeOfElement(), result);
            CpUtils.assertEquals("Name of type of first arg of call", "GetterAndSetter", ((ClassType) type).getName(),
                    result);
        }
    }

    /*checks if an array is correctly initialized*/
    @Test
    public void section5_Arrays_Init_Array() {
        var result = getOllirResult("arrays/ArrayInit.jmm");

        var method = CpUtils.getMethod(result, "main");

        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);

        CpUtils.assertEquals("Number of calls", 3, calls.size(), result);

        // Get new
        var newCalls = calls.stream().filter(call -> call.getInvocationType() == CallType.NEW)
                .collect(Collectors.toList());

        CpUtils.assertEquals("Number of 'new' calls", 1, newCalls.size(), result);

        // Get length
        var lengthCalls = calls.stream().filter(call -> call.getInvocationType() == CallType.arraylength)
                .collect(Collectors.toList());

        CpUtils.assertEquals("Number of 'arraylenght' calls", 1, lengthCalls.size(), result);
    }

    /*checks if the access to the elements of array is correct*/
    @Test
    public void section5_Arrays_Access_Array() {
        var result = getOllirResult("arrays/ArrayAccess.jmm");

        var method = CpUtils.getMethod(result, "foo");

        var assigns = CpUtils.assertInstExists(AssignInstruction.class, method, result);
        var numArrayStores = assigns.stream().filter(assign -> assign.getDest() instanceof ArrayOperand).count();
        CpUtils.assertEquals("Number of array stores", 5, numArrayStores, result);

        var numArrayReads = assigns.stream()
                .flatMap(assign -> CpUtils.getElements(assign.getRhs()).stream())
                .filter(element -> element instanceof ArrayOperand).count();
        CpUtils.assertEquals("Number of array reads", 5, numArrayReads, result);
    }

    /*checks multiple expressions as indexes to access the elements of an array*/
    @Test
    public void section5_Arrays_Load_ComplexArrayAccess() {
        // Just parse
        var result = getOllirResult("arrays/ComplexArrayAccess.jmm");

        var method = CpUtils.getMethod(result, "main");

        var assigns = CpUtils.assertInstExists(AssignInstruction.class, method, result);
        var numArrayStores = assigns.stream().filter(assign -> assign.getDest() instanceof ArrayOperand).count();
        CpUtils.assertEquals("Number of array stores", 5, numArrayStores, result);

        var numArrayReads = assigns.stream()
                .flatMap(assign -> CpUtils.getElements(assign.getRhs()).stream())
                .filter(element -> element instanceof ArrayOperand).count();
        CpUtils.assertEquals("Number of array reads", 6, numArrayReads, result);
    }

}
