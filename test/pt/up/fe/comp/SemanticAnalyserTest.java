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
        result = TestUtils.analyse("class dummy {int b; public int foo(){b = 0; return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(){int b; b = 0; b = 2; return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(){int b; b = 2; return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {int c; public int bar(int a, int b){return 0;} public int foo(){int x; x = 0; return this.bar(x,c);}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {" +
                "int b;" +
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
                "int b;" +
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
        result = TestUtils.analyse("class dummy extends other {public int bar(){ this.foo(); return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int bar(){ dummy a; a.bar(); return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("import Animal.Dog; class dummy {public int bar(Dog a){a.bark(); return 0;}}");
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testMethodCallError() {
        JmmSemanticsResult result = TestUtils.analyse("class dummy {" +
                "int b;" +
                "public int bar(int a){this.foo(a); return 0;}" +
                "}");
        TestUtils.mustFail(result.getReports());
    }

    @Test
    public void testSimpleTypeCheckCorrect() {
        JmmSemanticsResult result = TestUtils.analyse("class dummy {bool a; public int foo(){ !a; return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {bool a; public int foo(){ a = false; !a; return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {int a; public int foo(){ a = 2; return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {bool a; public int foo(){ a = true; bool b; b = !a; a = false || true && b; !a; return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {int a; public int foo(){ int b; bool c; b = 5; a = 2 + 7 - b * 2; c = b < a; return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {int a; public int foo(){ int b; b = 5; a = 2 + 7 - b * 2; return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(int[] a){a[123] = 0; return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(int[] a){a[10] = 5 + a[1]; return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy extends other {int a; public int foo(){ int b; bool c; a = this.hello(b,c); return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy extends other {int a; public int foo(){ a = this.hello()[2]; return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy extends other {int a; public int[] hello(){ int[] b; return b; } public int foo(){ a = this.hello()[2]; return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy extends other {int a; public int hello(){ int[] b; b[2] = this.a; return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy extends other { public int hello(){ int[] b; b[2] = this.a; return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {int a; public int hello(){ other b; a = b.intA; return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(int[] a){some b; a[2] = b.c().d()[5].f()[10]; return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(){ int[] a; a = new int[5]; return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(){ ola a; a = new ola(); return 0; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(){ int a; if (5 < 6) { a = 5; } else { a = 6; } return a; }}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(){ bool b; int a; while (b) { a = a + 1; } return 0; }}");
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testSimpleTypeCheckError() {
        JmmSemanticsResult result = TestUtils.analyse("class dummy {int a; public int foo(){ !a; return 0; }}"); //this.foo()[1];
        TestUtils.mustFail(result.getReports());
        result = TestUtils.analyse("class dummy {bool a; public int foo(){ a = 2; !a; return 0; }}");
        TestUtils.mustFail(result.getReports());
        result = TestUtils.analyse("class dummy {int a; public int foo(){ a = true; return 0; }}");
        TestUtils.mustFail(result.getReports());
        result = TestUtils.analyse("class dummy {bool a; public int foo(){ a = true; bool b; b = !a; a = false || 2 && b; !a; return 0; }}");
        TestUtils.mustFail(result.getReports());
        result = TestUtils.analyse("class dummy {int a; public int foo(){ int b; bool c; b = 5; a = 2 + false - b * 2; c = b < a; return 0; }}");
        TestUtils.mustFail(result.getReports());
        result = TestUtils.analyse("class dummy {int a; public int foo(){ int b; bool c; b = 5; a = 2 + 7 - b * 2; c = false < a; return 0; }}");
        TestUtils.mustFail(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(int[] a){a[false] = 0; return 0;}}");
        TestUtils.mustFail(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(int[] a){int b; a[10] = 5 + b[1]; return 0;}}");
        TestUtils.mustFail(result.getReports());
        result = TestUtils.analyse("class dummy extends other {int a; public int hello(){ return 0; } public int foo(){ a = this.hello()[2]; return 0; }}");
        TestUtils.mustFail(result.getReports());
        result = TestUtils.analyse("class dummy { public int hello(){ int[] b; b[2] = this.a; return 0; }}");
        TestUtils.mustFail(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(){ int a; if (5 + 6) { a = 5; } else { a = 6; } return a; }}");
        TestUtils.mustFail(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(){ bool b; int a; while (a * 2) { a = a + 1; } return 0; }}");
        TestUtils.mustFail(result.getReports());
    }
}
