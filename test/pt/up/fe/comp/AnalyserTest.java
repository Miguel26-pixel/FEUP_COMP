package pt.up.fe.comp;

import org.junit.Test;

public class AnalyserTest {

    private static void noErrors(String code) {
        var result = TestUtils.analyse(code);
        TestUtils.noErrors(result.getReports());
        System.out.println("\n---------\n");
    }

    @Test
    public void testImport() {
        noErrors("import a; import b; import c.d; class dummy {}");
    }

    @Test
    public void testMethodDeclaration() {
        noErrors("class test{public int f1(int p1){return 0;} public String f2(bool p1){return true;}}");
    }

    @Test
    public void testMainMethodDeclaration() {
        noErrors("class test{public static void main(String[] args){}}");
    }

    @Test
    public void testMixedMethodDeclaration() {
        noErrors("class test{public static void main(String[] args){} public String f2(bool p1){return true;}}");
    }
}
