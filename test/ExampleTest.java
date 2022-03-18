import org.junit.Test;

import pt.up.fe.comp.TestUtils;

public class ExampleTest {

    @Test
    public void testExpression() {
        TestUtils.parse("2+3\n10+20\n");
        // var parserResult = TestUtils.parse("2+3\n10+20\n");
        // parserResult.getReports().get(0).getException().get().printStackTrace();
        // // System.out.println();
        // var analysisResult = TestUtils.analyse(parserResult);
    }

}
