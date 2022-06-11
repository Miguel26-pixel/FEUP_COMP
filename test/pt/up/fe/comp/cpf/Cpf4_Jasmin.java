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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.Test;

import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.utilities.LineStream;

public class Cpf4_Jasmin {

    private static boolean USE_OLLIR_EXPERIMENTAL = false;

    public static void enableOllirInputs() {
        USE_OLLIR_EXPERIMENTAL = true;
    }

    public static boolean useOllirInputs() {
        return USE_OLLIR_EXPERIMENTAL;
    }

    static JasminResult getJasminResult(String filename) {
        if (USE_OLLIR_EXPERIMENTAL) {
            filename = SpecsIo.removeExtension(filename) + ".ollir";
            return TestUtils.backend(new OllirResult(SpecsIo.getResource("fixtures/public/cpf/4_jasmin/" + filename),
                    Collections.emptyMap()));
        }

        return TestUtils.backend(SpecsIo.getResource("fixtures/public/cpf/4_jasmin/" + filename));
    }

    /**
     * Test the declaration of the class
     */
    @Test
    public void section1_Basic_Structure_class() {

        JasminResult jasminResult = getJasminResult("basic/Structure_class.jmm");

        CpUtils.matches(jasminResult, "\\.class\\s+(public\\s+)?Structure_class");
    }

    /**
     * Test the declaration of the super class
     */
    @Test
    public void section1_Basic_Structure_superclass() {
        JasminResult jasminResult = getJasminResult("basic/Structure_class.jmm");
        CpUtils.matches(jasminResult, "\\.super\\s+java/lang/Object");
    }

    /**
     * Test the declaration of the class constructor calling the constructor of the superclass
     */
    @Test
    public void section1_Basic_Structure_constructor() {
        JasminResult jasminResult = getJasminResult("basic/Structure_class.jmm");
        CpUtils.matches(jasminResult, "\\.method\\s+(public\\s+)?<init>\\(\\)V");

        CpUtils.matches(jasminResult, "aload(\\s+|_)0");

        CpUtils.matches(jasminResult,
                "(invokespecial|invokenonvirtual)\\s+java/lang/Object(\\.|/)<init>\\(\\)V");
        CpUtils.assertTrue("Could not find return", jasminResult.getJasminCode().contains("return"), jasminResult);
        CpUtils.matches(jasminResult, "\\.end\\s+method");
        // System.out.println(jasminResult);
    }

    /**
     * Test the declaration of the main class
     */
    @Test
    public void section1_Basic_Structure_main() {
        JasminResult jasminResult = getJasminResult("basic/Structure_class.jmm");
        CpUtils.matches(jasminResult,
                "\\.method\\s+public\\s+static\\s+main\\(\\[Ljava/lang/String;\\)V");
    }

    /**
     * Test the declaration of class fields of type int
     */
    @Test
    public void section1_Basic_Structure_fields_int() {
        JasminResult jasminResult = getJasminResult("basic/Structure_fields.jmm");
        CpUtils.jasminHasField(jasminResult, "I");
    }

    /**
     * Test the declaration of class fields of type boolean
     */
    @Test
    public void section1_Basic_Structure_fields_boolean() {
        JasminResult jasminResult = getJasminResult("basic/Structure_fields.jmm");
        CpUtils.jasminHasField(jasminResult, "Z");
    }

    /**
     * Test the declaration of class fields of type int[]
     */
    @Test
    public void section1_Basic_Structure_fields_int_arr() {
        JasminResult jasminResult = getJasminResult("basic/Structure_fields.jmm");
        CpUtils.jasminHasField(jasminResult, "\\[I");
    }

    private static final String JASMIN_METHOD_REGEX_PREFIX = "\\.method\\s+((public|private)\\s+)?(\\w+)\\(\\)";

    /*checks if method declaration is correct (int)*/
    @Test
    public void section1_Basic_Method_Declaration_Int() {
        JasminResult jasminResult = getJasminResult("basic/BasicMethodsInt.jmm");
        CpUtils.matches(jasminResult, JASMIN_METHOD_REGEX_PREFIX + "I");
    }

    /*checks if method declaration is correct (boolean)*/
    @Test
    public void section1_Basic_Method_Declaration_Boolean() {
        JasminResult jasminResult = getJasminResult("basic/BasicMethodsBool.jmm");
        CpUtils.matches(jasminResult, JASMIN_METHOD_REGEX_PREFIX + "Z");
    }

    /*checks if method declaration is correct (class)*/
    @Test
    public void section1_Basic_Method_Declaration_Class() {
        JasminResult jasminResult = getJasminResult("basic/BasicMethodsClass.jmm");
        CpUtils.matches(jasminResult, JASMIN_METHOD_REGEX_PREFIX + "'?LBasicMethods;'?");
    }

    /*checks if method declaration is correct (array)*/
    @Test
    public void section1_Basic_Method_Declaration_Array() {
        JasminResult jasminResult = getJasminResult("basic/BasicMethodsArray.jmm");
        CpUtils.matches(jasminResult, JASMIN_METHOD_REGEX_PREFIX + "\\[I");
    }

    /*checks if the index for loading a argument is correct (should be 1) */
    @Test
    public void section2_Arithmetic_BytecodeIndex_IloadArg() {
        var jasminResult = getJasminResult("arithmetic/ByteCodeIndexes1.jmm");
        var methodCode = CpUtils.getJasminMethod(jasminResult);

        int iloadIndex = CpUtils.getBytecodeIndex("iload", methodCode);
        assertEquals(1, iloadIndex);
    }

    /*checks if the index for storing a var is correct (should be > 1) */
    @Test
    public void section2_Arithmetic_BytecodeIndex_IstoreVar() {
        var jasminResult = getJasminResult("arithmetic/ByteCodeIndexes2.jmm");
        var methodCode = CpUtils.getJasminMethod(jasminResult);

        int istoreIndex = CpUtils.getBytecodeIndex("istore", methodCode);
        assertTrue("Expected index to be greater than one, is " + istoreIndex, istoreIndex > 1);
    }

    /**
     * Test the code of an addition: Variables are loaded with iload iadd is called The result is stored using istore
     */
    @Test
    public void section2_Arithmetic_Simple_add() {
        CpUtils.runJasmin(getJasminResult("arithmetic/Arithmetic_add.jmm"), "5");
    }

    /**
     * Test the code of a subtraction: Variables are loaded with iload isub is called The result is stored using istore
     */
    @Test
    public void section2_Arithmetic_Simple_sub() {
        CpUtils.runJasmin(getJasminResult("arithmetic/Arithmetic_sub.jmm"), "3");
    }

    /**
     * Test the code of a multiplication: Variables are loaded with iload imul is called The result is stored using
     * istore
     */
    @Test
    public void section2_Arithmetic_Simple_mul() {
        CpUtils.runJasmin(getJasminResult("arithmetic/Arithmetic_mul.jmm"), "22");
    }

    /**
     * Test the code of an integer division: Variables are loaded with iload idiv is called The result is stored using
     * istore
     */
    @Test
    public void section2_Arithmetic_Simple_div() {
        CpUtils.runJasmin(getJasminResult("arithmetic/Arithmetic_div.jmm"), "5");
    }

    @Test
    public void section2_Arithmetic_Simple_and() {
        CpUtils.runJasmin(getJasminResult("arithmetic/Arithmetic_and.jmm"), "0");
    }

    @Test
    public void section2_Arithmetic_Simple_less() {
        CpUtils.runJasmin(getJasminResult("arithmetic/Arithmetic_less.jmm"), "1");
    }

    /*checks if an addition is correct (more than 2 values)*/
    @Test
    public void section2_Arithmetic_Complex_Add() {
        CpUtils.runJasmin(getJasminResult("arithmetic/ComplexAdd.jmm"), "Result: 19");
    }

    /*checks if a multiplication is correct (more than 2 values)*/
    @Test
    public void section2_Arithmetic_Complex_Prod() {
        CpUtils.runJasmin(getJasminResult("arithmetic/ComplexProd.jmm"), "Result: 196");
    }

    /*checks if a combination of addition and multiplication is correct (checks precedences)*/
    @Test
    public void section2_Arithmetic_Complex_AddMul() {
        CpUtils.runJasmin(getJasminResult("arithmetic/ComplexAddMul.jmm"), "Result: 73");
    }

    /*checks if a combination of subtraction and division is correct (checks precedences)*/
    @Test
    public void section2_Arithmetic_Complex_SubDiv() {
        CpUtils.runJasmin(getJasminResult("arithmetic/ComplexSubDiv.jmm"), "Result: 5");
    }

    /*checks if a more complex arithmetic expression (multiple different operations) is correct*/
    @Test
    public void section2_Arithmetic_Complex_Arithmetic() {
        CpUtils.runJasmin(getJasminResult("arithmetic/ComplexArithmetic.jmm"), "Result: 168");
    }

    /*checks if an addition is correct (more than 2 values)*/
    @Test
    public void section3_ControlFlow_If_Simple() {
        CpUtils.runJasmin(getJasminResult("control_flow/SimpleIfElseStat.jmm"), "Result: 5\nResult: 8");
    }

    /*checks if an addition is correct (more than 2 values)*/
    @Test
    public void section3_ControlFlow_If_Not_Simple() {
        CpUtils.runJasmin(getJasminResult("control_flow/SimpleIfElseNot.jmm"), "10\n200");
    }

    /*checks if the code of a simple WHILE statement is well executed */
    @Test
    public void section3_ControlFlow_While_Simple() {
        CpUtils.runJasmin(getJasminResult("control_flow/SimpleWhileStat.jmm"), "Result: 0\nResult: 1\nResult: 2");
    }

    /*checks if the code of a more complex IF ELSE statement (similar a switch statement) is well executed */
    @Test
    public void section3_ControlFlow_Mixed_Switch() {
        CpUtils.runJasmin(getJasminResult("control_flow/SwitchStat.jmm"),
                "Result: 1\nResult: 2\nResult: 3\nResult: 4\nResult: 5\nResult: 6\nResult: 7");
    }

    /*checks if the code of a more complex IF ELSE statement (similar a switch statement) is well executed */
    @Test
    public void section3_ControlFlow_Mixed_Nested() {
        CpUtils.runJasmin(getJasminResult("control_flow/IfWhileNested.jmm"), "Result: 1\nResult: 2\nResult: 1");
    }

    /*checks if dynamic invocations work properly*/
    @Test
    public void section4_Calls_Invoke_Virtual() {
        var jasminResult = getJasminResult("calls/NoArgsFuncCall.jmm");
        var methodCode = CpUtils.getJasminMethod(jasminResult);

        CpUtils.matches(methodCode, "invokevirtual\\s+NoArgsFuncCall(/|\\.)bar");
    }

    /*checks if static invocations work properly*/
    @Test
    public void section4_Calls_Invoke_Static() {
        var jasminResult = getJasminResult("calls/InvokeStatic.jmm");
        var methodCode = CpUtils.getJasminMethod(jasminResult);

        CpUtils.matches(methodCode, "invokestatic\\s+ioPlus(/|\\.)printResult");

    }

    /*checks if the code of a call to a function with one argument is well executed*/
    @Test
    public void section4_Calls_Misc_OneArg() {
        CpUtils.runJasmin(getJasminResult("calls/OneArgFuncCall.jmm"), "Result: 10");

    }

    /*checks if the code of a call to a function with multiple arguments (using variables in the call) is well
    executed*/
    @Test
    public void section4_Calls_Misc_VarArgs() {
        CpUtils.runJasmin(getJasminResult("calls/VarArgsFuncCall.jmm"), "Result: 10\nResult: 12\nResult: 11");

    }

    /*checks if the code of a call to a function with multiple arguments (using arithmetic expressions in the call)
    is well executed*/
    @Test
    public void section4_Calls_Misc_ArithmeticArgs() {
        CpUtils.runJasmin(getJasminResult("calls/ArithmeticArgsFuncCall.jmm"), "Result: 0\nResult: 15\nResult: 5");

    }

    /*checks if the code of a call to a function with multiple arguments (using boolean expressions in the call) is
    well executed*/
    @Test
    public void section4_Calls_Misc_ConditionArgs() {
        CpUtils.runJasmin(getJasminResult("calls/ConditionArgsFuncCall.jmm"), "Result: 10");

    }

    /*checks if the code of a call to a function with multiple arguments (using other calls to functions in the call)
    is well executed*/
    @Test
    public void section4_Calls_Misc_FunctionArgs() {
        CpUtils.runJasmin(getJasminResult("calls/FuncArgsFuncCall.jmm"), "Result: 10\nResult: 5");
    }

    /*checks if pop is being used when required*/
    @Test
    public void section4_Calls_UsesPop() {
        var jasminResult = getJasminResult("calls/UsesPop.jmm");
        var methodCode = CpUtils.getJasminMethod(jasminResult);

        // Find invoke virtual, check what kind of instruction comes after it
        try (var lines = LineStream.newInstance(methodCode)) {
            while (lines.hasNextLine()) {
                var line = lines.nextLine().strip();

                if (!line.startsWith("invokevirtual")) {
                    continue;
                }

                // Found invoke, check next line
                var afterInvoke = lines.nextLine().strip();

                CpUtils.assertTrue(
                        "After an invokevirtual, expected an instruction that consumes an element from the stack, but found '"
                                + afterInvoke
                                + "', in the following method:\n"
                                + methodCode + "\n\n",
                        afterInvoke.startsWith("istore") || afterInvoke.equals("pop"), jasminResult);
                return;
            }

            fail("Could not find invokevirtual instruction in method:\n\n" + methodCode);
        }
    }

    /*checks if the code of a call to a function with one argument is well executed*/
    @Test
    public void section4_Calls_SetInlineAndPrintOtherClassInline() {
        CpUtils.runJasmin(getJasminResult("calls/PrintOtherClassInline.jmm"), "10");
    }

    /*checks if an array is correctly initialized*/
    @Test
    public void section5_Arrays_Init_Array() {
        CpUtils.runJasmin(getJasminResult("arrays/ArrayInit.jmm"), "Result: 5");

    }

    /*checks if the access to the elements of array is correct*/
    @Test
    public void section5_Arrays_Store_Array() {
        CpUtils.runJasmin(getJasminResult("arrays/ArrayAccess.jmm"),
                "Result: 1\nResult: 2\nResult: 3\nResult: 4\nResult: 5");

    }

    /*checks multiple expressions as indexes to access the elements of an array*/
    @Test
    public void section5_Arrays_Load_ComplexArrayAccess() {
        CpUtils.runJasmin(getJasminResult("arrays/ComplexArrayAccess.jmm"),
                "Result: 1\nResult: 2\nResult: 3\nResult: 4\nResult: 5");

    }

    /*checks if array has correct signature ?*/
    @Test
    public void section5_Arrays_Signature_ArrayAsArg() {
        var jasminResult = getJasminResult("arrays/ArrayAsArgCode.jmm");
        var methodCode = CpUtils.getJasminMethod(jasminResult);

        CpUtils.matches(methodCode, "invokevirtual\\s+ArrayAsArg(/|\\.)(\\w+)\\(\\[I\\)I");
    }

    /*checks if array is being passed correctly as an argument to a function*/
    @Test
    public void section5_Arrays_As_Arg_Simple() {
        CpUtils.runJasmin(getJasminResult("arrays/ArrayAsArg.jmm"), "Result: 2");
    }

    /*checks if array is being passed correctly as an argument to a function (index of aload > 1)*/
    @Test
    public void section5_Arrays_As_Arg_Aload() {
        var jasminResult = getJasminResult("arrays/ArrayAsArgCode.jmm");
        var methodCode = CpUtils.getJasminMethod(jasminResult);

        int aloadIndex = CpUtils.getBytecodeIndex("aload", methodCode);
        assertTrue("Expected aload index to be greater than 1, is " + aloadIndex + ":\n" + methodCode, aloadIndex > 1);
    }

    /*checks if the .limits locals is not a const 99 value */
    @Test
    public void section6_Limits_Locals_Not_99() {
        var jasminResult = getJasminResult("limits/LocalLimits.jmm");
        var methodCode = CpUtils.getJasminMethod(jasminResult);
        var numLocals = Integer.parseInt(SpecsStrings.getRegexGroup(methodCode, CpUtils.getLimitLocalsRegex(), 1));
        assertTrue("limit locals should be less than 99:\n" + methodCode, numLocals >= 0 && numLocals < 99);

        // Make sure the code compiles
        jasminResult.compile();
    }

    /*checks if the .limits locals is the expected value (with a tolerance of 2) */
    @Test
    public void section6_Limits_Locals_Simple() {

        var jasminResult = getJasminResult("limits/LocalLimits.jmm");
        var methodCode = CpUtils.getJasminMethod(jasminResult);
        var numLocals = Integer.parseInt(SpecsStrings.getRegexGroup(methodCode, CpUtils.getLimitLocalsRegex(), 1));

        // Find store or load with numLocals - 1
        var regex = CpUtils.getLocalsRegex(numLocals);
        CpUtils.matches(methodCode, regex);

        // Makes sure the code compiles
        jasminResult.compile();
    }

    /*checks if the .limits stack is not a const 99 value */
    @Test
    public void section6_Limits_Stack_Not_99() {
        var jasminResult = getJasminResult("limits/LocalLimits.jmm");
        var methodCode = CpUtils.getJasminMethod(jasminResult);
        var numStack = Integer.parseInt(SpecsStrings.getRegexGroup(methodCode, CpUtils.getLimitStackRegex(), 1));
        assertTrue("limit stack should be less than 99:\n" + methodCode, numStack >= 0 && numStack < 99);

        // Make sure the code compiles
        jasminResult.compile();
    }

    /*checks if the .limits stack is the expected value (with a tolerance of 2) */
    @Test
    public void section6_Limits_Stack_Simple() {

        var jasminResult = getJasminResult("limits/LocalLimits.jmm");
        var methodCode = CpUtils.getJasminMethod(jasminResult);
        var numStack = Integer.parseInt(SpecsStrings.getRegexGroup(methodCode, CpUtils.getLimitStackRegex(), 1));

        int expectedLimit = 3;
        int errorMargin = 2;
        int upperLimit = expectedLimit + errorMargin;

        assertTrue(
                "limit stack should be = " + expectedLimit + " (accepted if <= " + upperLimit
                        + "), but is " + numStack + ":\n" + methodCode,
                numStack <= upperLimit && numStack >= expectedLimit);

        // Make sure the code compiles
        jasminResult.compile();
    }
}
