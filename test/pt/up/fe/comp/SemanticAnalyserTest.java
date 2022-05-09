package pt.up.fe.comp;

import org.junit.Test;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;

public class SemanticAnalyserTest {
    @Test
    public void twoMainMethods() {
        JmmSemanticsResult result = TestUtils.analyse("class dummy {public static void main(String[] args){} public static void main(String[] args){}}");
        TestUtils.mustFail(result.getReports());
    }

    @Test
    public void testVariableExistenceCorrect() {
        JmmSemanticsResult result = TestUtils.analyse("class dummy {public int foo(int a){a = 0; return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {int b = 0; public int foo(){b = 0; return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(){int b = 0; b = 2; return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(){int b; b = 2; return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {int c; public int bar(int a, int b){return 0;} public int foo(){int x = 0; return this.bar(x,c);}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {" +
                "int b = 5;" +
                "public int foo(int a){a = 5; return 0;}" +
                "public int bar(int a){this.foo(b); return 0;}" +
                "}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {String a; public static void main(String[] args){args[0] = this.a;}}");
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testVariableExistenceError() {
        JmmSemanticsResult result = TestUtils.analyse("class dummy {public int foo(int a){b = 0; return 0;}}");
        TestUtils.mustFail(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(int a){b = 0; return 0;}}");
        TestUtils.mustFail(result.getReports());

        result = TestUtils.analyse("class dummy {" +
                "public int foo(int a){a = 5 + b; return 0;}" +
                "public int bar(){this.foo(); return 0;}" +
                "}");
        TestUtils.mustFail(result.getReports());

        result = TestUtils.analyse("class dummy {" +
                "public int foo(int a){a = 5; return 0;}" +
                "public int bar(int a){this.foo(b); return 0;}" +
                "}");
        TestUtils.mustFail(result.getReports());

        result = TestUtils.analyse("class dummy {" +
                "public int foo(int a){a = 5; return 0;}" +
                "public int bar(int a){this.foo(a); b[2] = 3; return 0;}" +
                "}");
        TestUtils.mustFail(result.getReports());

        result = TestUtils.analyse("class dummy {" +
                "int b = 3;" +
                "public int foo(int a){a = 5; return 0;}" +
                "public int bar(int a){this.foo(a); b[2] = 3; return 0;}" +
                "}");
        TestUtils.mustFail(result.getReports());
    }

    @Test
    public void arrayAccessOverArray() {
        JmmSemanticsResult result = TestUtils.analyse("class dummy {public int foo(int[] a){a[2] = 0; return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(int a){a[2] = 0; return 0;}}");
        TestUtils.mustFail(result.getReports());
    }

    @Test
    public void testMethodCallCorrect() {
        JmmSemanticsResult result = TestUtils.analyse("class dummy {public int foo(){return 0;} public int bar(){this.foo(); return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy extends other {public int bar(){this.foo(); return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("import Animal.Dog; class dummy {public int bar(Dog a){a.bark(); return 0;}}");
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testMethodCallError() {
        JmmSemanticsResult result = TestUtils.analyse("class dummy {" +
                "int b = 3;" +
                "public int bar(int a){this.foo(a); return 0;}" +
                "}");
        TestUtils.mustFail(result.getReports());
    }

    @Test
    public void testAssignTypeCorrect() {
        /*JmmSemanticsResult result = TestUtils.analyse(
            "class dummy {" +
                    "int a;" +
                    "public int foo() {" +
                        "a = 0;" +
                        "a = 5;" +
                        "return 0;" +
                    "}" +
                "}");
        TestUtils.noErrors(result.getReports());*/

        /*result = TestUtils.analyse(
                "class dummy {" +
                        "int a;" +
                        "public int foo() {" +
                        "a = 2;" +
                        "a = 5 + a + (4 * 3);" +
                        "return 0;" +
                        "}" +
                        "}");
        TestUtils.noErrors(result.getReports());*/

        JmmSemanticsResult result = TestUtils.analyse(
                "class dummy {" +
                        "int[] a = new int[5];" +
                        "public int[] bar() {int[] a; return a;}" +
                        "public int foo() {" +
                        //"a = 5 + a.add() + (4 * 3);" +
                        "int b;" +
                        "a[2] = 0;" +
                        "b = 5 + a[3] + (4 * 3);" +
                        //"b = 5 + this.bar()[1] + (4 * 3);" +
                        "return 0;" +
                        "}" +
                        "}");
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testAssignTypeError() {
        JmmSemanticsResult result = TestUtils.analyse(
                "class dummy {" +
                        "int a;" +
                        "public int foo() {" +
                        "a = 0;" +
                        "a = false;" +
                        "return 0;" +
                        "}" +
                        "}");
        TestUtils.mustFail(result.getReports());

        result = TestUtils.analyse(
                "class dummy {" +
                        "int a;" +
                        "public int foo() {" +
                        "a = 0;" +
                        "a = false;" +
                        "return 0;" +
                        "}" +
                        "}");
        TestUtils.mustFail(result.getReports());

        result = TestUtils.analyse(
                "class dummy {" +
                        "int[] a = new int[5];" +
                        "public int foo() {" +
                        "a = 2;" +
                        "a = 5 + a + (4 * 3);" +
                        "return 0;" +
                        "}" +
                        "}");
        TestUtils.mustFail(result.getReports());
    }
}
