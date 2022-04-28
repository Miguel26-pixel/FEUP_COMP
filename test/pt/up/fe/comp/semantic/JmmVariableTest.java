package pt.up.fe.comp.semantic;

import org.junit.Test;
import pt.up.fe.comp.semantic.JmmVariable;

import static org.junit.Assert.assertEquals;

public class JmmVariableTest {

    @Test
    public void testGetType() {
        JmmVariable jmmv = new JmmVariable("typeTest", "b");

        assertEquals(jmmv.getType(), "typeTest");
    }

    @Test
    public void testGetName() {
        JmmVariable jmmv = new JmmVariable("a", "nameTest");

        assertEquals(jmmv.getName(), "nameTest");
    }
}
