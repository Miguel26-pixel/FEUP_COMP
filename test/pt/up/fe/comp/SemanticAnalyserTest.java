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
    public void arrayAccessOverArray() {
        JmmSemanticsResult result = TestUtils.analyse("class dummy {public int foo(int[] a){a[2] = 0; return 0;}}");
        TestUtils.noErrors(result.getReports());
        result = TestUtils.analyse("class dummy {public int foo(int a){a[2] = 0; return 0;}}");
        TestUtils.mustFail(result.getReports());
    }
}
