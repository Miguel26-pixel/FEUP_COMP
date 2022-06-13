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

package pt.up.fe.comp.cpf;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

public class Cpf5_Optimizations {

    static OllirResult getOllirResult(String filename) {
        return TestUtils.optimize(SpecsIo.getResource("fixtures/public/cpf/5_optimizations/" + filename));
    }

    static JasminResult getJasminResult(String filename) {
        String resource = SpecsIo.getResource("fixtures/public/cpf/5_optimizations/" + filename);
        return TestUtils.backend(resource);
    }

    static JasminResult getJasminResultOpt(String filename) {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        return TestUtils.backend(SpecsIo.getResource("fixtures/public/cpf/5_optimizations/" + filename), config);
    }

    static JasminResult getJasminResultReg(String filename, int numReg) {
        Map<String, String> config = new HashMap<>();
        config.put("registerAllocation", String.valueOf(numReg));
        return TestUtils.backend(SpecsIo.getResource("fixtures/public/cpf/5_optimizations/" + filename), config);
    }

    /**
     * Test if small integers are loaded with iconst
     */
    @Test
    public void section1_InstSelection_iconst_0() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_iconst_0.jmm");
        CpUtils.matches(jasminResult, "iconst_0");

    }

    @Test
    public void section1_InstSelection_iconst_5() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_iconst_5.jmm");
        CpUtils.matches(jasminResult, "iconst_5");
    }

    /**
     * Test if integer 6 is loaded with bipush
     */
    @Test
    public void section1_InstSelection_bipush_6() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_bipush_6.jmm");
        CpUtils.matches(jasminResult, "bipush\\s6");
    }

    /**
     * Test if integer 127 is loaded with bipush
     */
    @Test
    public void section1_InstSelection_bipush_127() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_bipush_127.jmm");
        CpUtils.matches(jasminResult, "bipush\\s127");
    }

    /**
     * Test if integer 128 is loaded with sipush
     */
    @Test
    public void section1_InstSelection_sipush_128() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_sipush_128.jmm");
        CpUtils.matches(jasminResult, "sipush\\s128");
    }

    /**
     * Test if integer 32767 is loaded with sipush
     */
    @Test
    public void section1_InstSelection_sipush_32767() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_sipush_32767.jmm");
        CpUtils.matches(jasminResult, "sipush\\s32767");
    }

    /**
     * Test if integer 32768 is loaded with ldc
     */
    @Test
    public void section1_InstSelection_ldc_32768() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_ldc_32768.jmm");
        CpUtils.matches(jasminResult, "ldc\\s32768");
    }

    @Test
    public void section1_InstSelection_IfLt() {
        var jasminResult = getJasminResult("inst_selection/InstSelection_if_lt.jmm");
        CpUtils.matches(jasminResult, "(iflt|ifge)");
    }

    /**
     * Test if iinc is used when incrementing a variable
     */
    @Test
    public void section1_InstSelection_iinc() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_iinc.jmm");
        CpUtils.matches(jasminResult, "iinc\\s+\\w+\\s+1");

    }

    /**
     * Test if iload_1 is used.
     */
    @Test
    public void section1_InstSelection_iload_1() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_load_1.jmm");
        CpUtils.matches(jasminResult, "iload_1");
    }

    /**
     * Test if iload_3 is used.
     */
    @Test
    public void section1_InstSelection_iload_3() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_load_3.jmm");
        CpUtils.matches(jasminResult, "iload_3");

    }

    /**
     * Test if iload 4 is used.
     */
    @Test
    public void section1_InstSelection_iload_4() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_load_4.jmm");
        CpUtils.matches(jasminResult, "iload\\s4");

    }

    /**
     * Test if istore_1 is used.
     */
    @Test
    public void section1_InstSelection_istore_1() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_store_1.jmm");
        CpUtils.matches(jasminResult, "istore_1");

    }

    /**
     * Test if istore_3 is used.
     */
    @Test
    public void section1_InstSelection_istore_3() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_store_3.jmm");
        CpUtils.matches(jasminResult, "istore_3");

    }

    /**
     * Test if istore 4 is used.
     */
    @Test
    public void section1_InstSelection_istore_4() {
        JasminResult jasminResult = getJasminResult("inst_selection/InstSelection_store_4.jmm");
        CpUtils.matches(jasminResult, "istore\\s4");

    }

    @Test
    public void section2_RegAlloc_AtMostRequestedNumber() {

        String filename = "reg_alloc/regalloc.jmm";
        int expectedNumReg = 3;

        JasminResult original = getJasminResult(filename);
        JasminResult optimized = getJasminResultReg(filename, expectedNumReg);

        CpUtils.assertNotEquals("Expected code to change with -r flag\n\nOriginal code:\n" + original.getJasminCode(),
                original.getJasminCode(), optimized.getJasminCode(),
                optimized);

        String method = CpUtils.getJasminMethod(optimized, "soManyRegisters");
        Pattern pattern = Pattern.compile("\\.limit\\s+locals\\s+(\\d+)\\s+");
        Matcher matcher = pattern.matcher(method);
        CpUtils.assertTrue("Expected to find correct .limit locals directive",
                matcher.find(),
                optimized);

        String captured = matcher.group(1);
        CpUtils.assertNotNull("Expected to find correct .limit locals directive",
                captured,
                optimized);

        Integer actualNumReg = SpecsStrings.decodeInteger(captured);
        CpUtils.assertNotNull("Could not convert locals limit to integer",
                actualNumReg,
                optimized);

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be <= than " + expectedNumReg,
                actualNumReg <= expectedNumReg,
                optimized);
    }

    @Test
    public void section2_RegAlloc_UsesMinimumPossible() {

        String filename = "reg_alloc/regalloc.jmm";

        JasminResult original = getJasminResult(filename);
        JasminResult optimized = getJasminResultReg(filename, 0);

        CpUtils.assertNotEquals("Expected code to change with -r flag\n\nOriginal code:\n" + original.getJasminCode(),
                original.getJasminCode(), optimized.getJasminCode(),
                optimized);

        String method = CpUtils.getJasminMethod(optimized, "soManyRegisters");
        CpUtils.matches(method, "\\.limit\\s+locals\\s+3");
    }

    @Test
    public void section3_ConstProp_Simple() {

        String filename = "const_prop/PropSimple.jmm";

        JasminResult original = getJasminResult(filename);
        JasminResult optimized = getJasminResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getJasminCode(),
                original.getJasminCode(), optimized.getJasminCode(),
                optimized);

        CpUtils.matches(optimized, "(bipush|sipush|ldc) 10\\s+ireturn");
    }

    @Test
    public void section3_ConstProp_InSequentialCode() {

        String filename = "const_prop/PropSequential.jmm";

        JasminResult original = getJasminResult(filename);
        JasminResult optimized = getJasminResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getJasminCode(),
                original.getJasminCode(), optimized.getJasminCode(),
                optimized);

        CpUtils.matches(optimized, "(bipush|sipush|ldc) 10\\s+iload(\\s|_)\\d+\\s+imul");
    }

    @Test
    public void section3_ConstProp_WithLoop() {

        String filename = "const_prop/PropWithLoop.jmm";

        JasminResult original = getJasminResult(filename);
        JasminResult optimized = getJasminResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getJasminCode(),
                original.getJasminCode(), optimized.getJasminCode(),
                optimized);

        CpUtils.matches(optimized, "(bipush|sipush|ldc) 10\\s+imul");
    }

    @Test
    public void section3_ConstProp_WithIf() {

        String filename = "const_prop/PropWithIf.jmm";

        JasminResult original = getJasminResult(filename);
        JasminResult optimized = getJasminResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getJasminCode(),
                original.getJasminCode(), optimized.getJasminCode(),
                optimized);

        CpUtils.matches(optimized, "(bipush|sipush|ldc) 10\\s+if_icmp");
    }

    @Test
    public void section4_WhileTemplate_DoWhile() {

        String filename = "while_template/WhileOpt.jmm";
        int expectedIf = 1;
        int expectedGoto = 0;

        JasminResult original = getJasminResult(filename);
        JasminResult optimized = getJasminResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getJasminCode(),
                original.getJasminCode(), optimized.getJasminCode(),
                optimized);

        var ifOccurOpt = CpUtils.countOccurences(optimized, "if_");
        var gotoOccurOpt = CpUtils.countOccurences(optimized, "goto");

        CpUtils.assertEquals("Expected exactly " + expectedIf + " if instruction", expectedIf, ifOccurOpt, optimized);
        CpUtils.assertEquals("Expected exactly " + expectedGoto + " goto instructions", expectedGoto, gotoOccurOpt,
                optimized);
    }
}
