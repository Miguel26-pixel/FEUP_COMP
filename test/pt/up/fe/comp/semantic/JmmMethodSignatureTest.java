package pt.up.fe.comp.semantic;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JmmMethodSignatureTest {

    @Test
    public void testGetReturnType() {
        JmmMethodSignature jmmms = new JmmMethodSignature("void", List.of());

        assertEquals(jmmms.getReturnType(), "void");
    }

    @Test
    public void testGetParametersLength() {
        JmmVariable var1Stub = Mockito.mock(JmmVariable.class);
        Mockito.when(var1Stub.getType()).thenReturn("int");
        Mockito.when(var1Stub.getName()).thenReturn("var1");

        JmmVariable var2Stub = Mockito.mock(JmmVariable.class);
        Mockito.when(var2Stub.getType()).thenReturn("bool");
        Mockito.when(var2Stub.getName()).thenReturn("var2");

        JmmMethodSignature jmmms = new JmmMethodSignature("", Arrays.asList(var1Stub, var2Stub));
        assertEquals(jmmms.getParameters().size(), 2);
    }

    @Test
    public void testGetParametersContent() {
        JmmVariable var1Stub = Mockito.mock(JmmVariable.class);
        Mockito.when(var1Stub.getType()).thenReturn("int");
        Mockito.when(var1Stub.getName()).thenReturn("var1");

        JmmVariable var2Stub = Mockito.mock(JmmVariable.class);
        Mockito.when(var2Stub.getType()).thenReturn("bool");
        Mockito.when(var2Stub.getName()).thenReturn("var2");

        JmmMethodSignature jmmms = new JmmMethodSignature("", Arrays.asList(var1Stub, var2Stub));

        List<JmmVariable> parameters = jmmms.getParameters();
        JmmVariable var1 = parameters.get(0);
        JmmVariable var2 = parameters.get(1);

        assertEquals(var1.getType(), "int");
        assertEquals(var2.getType(), "bool");
        assertEquals(var1.getName(), "var1");
        assertEquals(var2.getName(), "var2");
    }

    @Test
    public void testDoParametersMatch() {
        JmmVariable var1Stub = Mockito.mock(JmmVariable.class);
        Mockito.when(var1Stub.getType()).thenReturn("int");
        Mockito.when(var1Stub.getName()).thenReturn("var1");

        JmmVariable var2Stub = Mockito.mock(JmmVariable.class);
        Mockito.when(var2Stub.getType()).thenReturn("bool");
        Mockito.when(var2Stub.getName()).thenReturn("var2");

        JmmVariable var3Stub = Mockito.mock(JmmVariable.class);
        Mockito.when(var3Stub.getType()).thenReturn("int");
        Mockito.when(var3Stub.getName()).thenReturn("var3");

        JmmVariable var4Stub = Mockito.mock(JmmVariable.class);
        Mockito.when(var4Stub.getType()).thenReturn("bool");
        Mockito.when(var4Stub.getName()).thenReturn("var4");

        JmmMethodSignature jmmms = new JmmMethodSignature("", Arrays.asList(var1Stub, var2Stub));
        assertTrue(jmmms.doParametersMatch(Arrays.asList(var3Stub, var4Stub)));
    }

    @Test
    public void testIsSameReturnType() {
        JmmVariable var1Stub = Mockito.mock(JmmVariable.class);
        Mockito.when(var1Stub.getType()).thenReturn("int");
        Mockito.when(var1Stub.getName()).thenReturn("var1");

        JmmMethodSignature jmmms = new JmmMethodSignature("int", List.of());

        assertTrue(jmmms.isSameReturnType(var1Stub));
    }
}
