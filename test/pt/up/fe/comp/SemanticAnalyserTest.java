package pt.up.fe.comp;

import org.junit.Test;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;

public class SemanticAnalyserTest {
    @Test
    public void twoMainMethods() {
        JmmSemanticsResult result = TestUtils.analyse("class dummy {public static void main(String[] args){} public static void main(String[] args){}}");
        TestUtils.mustFail(result.getReports());
    }
}
