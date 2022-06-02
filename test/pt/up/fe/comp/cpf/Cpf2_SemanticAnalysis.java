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

import org.junit.Test;

import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.specs.util.SpecsIo;

public class Cpf2_SemanticAnalysis {

    static JasminResult getJasminResult(String filename) {
        return TestUtils.backend(SpecsIo.getResource("fixtures/public/cpf/2_semantic_analysis/" + filename));
    }

    static JmmSemanticsResult getSemanticsResult(String filename) {
        return TestUtils.analyse(SpecsIo.getResource("fixtures/public/cpf/2_semantic_analysis/" + filename));
    }

    static JmmSemanticsResult test(String filename, boolean fail) {
        var semantics = getSemanticsResult(filename);
        if (fail) {
            TestUtils.mustFail(semantics.getReports());
        } else {
            TestUtils.noErrors(semantics.getReports());
        }
        return semantics;
    }

    @Test
    public void section1_SymbolTable_Fields() {
        var semantics = test("symboltable/MethodsAndFields.jmm", false);
        var st = semantics.getSymbolTable();
        var fields = st.getFields();
        assertEquals(3, fields.size());
        var checkInt = 0;
        var checkBool = 0;
        var checkObj = 0;
        System.out.println("FIELDS: " + fields);
        for (var f : fields) {
            switch (f.getType().getName()) {
            case "MethodsAndFields":
                checkObj++;
                break;
            case "boolean":
                checkBool++;
                break;
            case "int":
                checkInt++;
                break;
            }
        }
        ;
        CpUtils.assertEquals("Field of type int", 1, checkInt, st);
        CpUtils.assertEquals("Field of type boolean", 1, checkBool, st);
        CpUtils.assertEquals("Field of type object", 1, checkObj, st);

    }

    @Test
    public void section1_SymbolTable_Parameters() {
        var semantics = test("symboltable/Parameters.jmm", false);
        var st = semantics.getSymbolTable();
        var methods = st.getMethods();
        CpUtils.assertEquals("Number of methods", 1, methods.size(), st);

        var parameters = st.getParameters(methods.get(0));
        CpUtils.assertEquals("Number of parameters", 3, parameters.size(), st);
        CpUtils.assertEquals("Parameter 1", "int", parameters.get(0).getType().getName(), st);
        CpUtils.assertEquals("Parameter 2", "boolean", parameters.get(1).getType().getName(), st);
        CpUtils.assertEquals("Parameter 3", "Parameters", parameters.get(2).getType().getName(), st);
    }

    /**
     * Test if fields are not being accessed from static methods.
     */
    @Test
    public void section2_Lookup_SuperWithImport() {
        var semantics = test("import/ImportSuper.jmm", false);
        CpUtils.assertEquals("Super", "Sup", semantics.getSymbolTable().getSuper(), semantics.getSymbolTable());
    }

    /**
     * Test if fields are not being accessed from static methods.
     */
    @Test
    public void section2_Lookup_VarLookup_Field_Main_Fail() {
        test("lookup/VarLookup_Field_Main_Fail.jmm", true);
    }

    /**
     * Test if the code can correctly lookup a local variable, even if there is a field with the same name.
     */
    @Test
    public void section2_Lookup_VarLookup_Local() {
        var jasminResult = getJasminResult("lookup/VarLookup_Local.jmm");

        assertEquals("10", jasminResult.run().trim());
    }

    /**
     * Test if the code can correctly lookup a field.
     */
    @Test
    public void section2_Lookup_VarLookup_Field() {
        var jasminResult = getJasminResult("lookup/VarLookup_Field.jmm");

        CpUtils.assertEquals("Lookup of field", "10", jasminResult.run().trim(), jasminResult);
    }

    @Test
    public void section3_TypeVerification_arraySumBad() {
        test("type_verification/Array_Sum_Bad.jmm", true);
    }

    @Test
    public void section3_TypeVerification_ArrayIndexBad() {
        test("type_verification/Array_Index_Bad.jmm", true);
    }

    @Test
    public void section3_TypeVerification_Assignment_Array_Bad() {
        test("type_verification/Assignment_Array_Bad.jmm", true);
    }

    @Test
    public void section3_TypeVerification_Assignment_Bool_Bad() {
        test("type_verification/Assignment_Bool_Bad.jmm", true);
    }

    @Test
    public void section3_TypeVerification_BinaryOps_Simple() {
        test("type_verification/BinaryOps_Simple.jmm", false);
    }

    @Test
    public void section3_TypeVerification_BinaryOps_BadInts() {
        test("type_verification/BinaryOps_BadInts.jmm", true);

    }

    @Test
    public void section3_TypeVerification_While_If_Array_WhileCondBool() {
        test("type_verification/While_If_Array_WhileCondBool.jmm", false);
    }

    @Test
    public void section3_TypeVerification_While_If_Array_IfCondNotBool() {
        test("type_verification/While_If_Array_IfCondNotBool.jmm", true);
    }

    @Test
    public void section3_TypeVerification_While_If_Array_AccessArrayNotInteger() {
        test("type_verification/While_If_Array_AccessArrayNotInteger.jmm", true);
    }

    @Test
    public void section4_CallsVerification_Inherited_Method_Call_Simple() {
        test("calls/Inherited_Method_Call_Simple.jmm", false);
    }

    @Test
    public void section4_CallsVerification_Method_Call_Missing() {
        test("calls/Method_Call_Missing.jmm", true);
    }

    @Test
    public void section4_CallsVerification_Other_Class_Method_Call_Simple() {
        test("calls/Other_Class_Method_Call_Simple.jmm", false);
    }

    @Test
    public void section5_ThisValidation_ThisAsCommonExpression() {
        test("this/ThisAsArg.jmm", false);
    }

    @Test
    public void section5_ThisValidation_ThisInMain() {
        test("this/ThisInMain.jmm", true);
    }
}
