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
}
