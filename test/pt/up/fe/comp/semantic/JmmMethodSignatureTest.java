package pt.up.fe.comp.semantic;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class JmmMethodSignatureTest {

    @Test
    public void testGetReturnType() {
        Type returnType = Mockito.mock(Type.class);
        Mockito.when(returnType.getName()).thenReturn("void");
        Mockito.when(returnType.isArray()).thenReturn(false);

        JmmMethodSignature jmmms = new JmmMethodSignature(returnType, List.of());

        assertEquals(jmmms.getReturnType().getName(), "void");
        assertFalse(jmmms.getReturnType().isArray());
    }

    @Test
    public void testGetParametersLength() {
        Type typeVar1 = Mockito.mock(Type.class);
        Mockito.when(typeVar1.getName()).thenReturn("int");
        Mockito.when(typeVar1.isArray()).thenReturn(false);

        Type typeVar2 = Mockito.mock(Type.class);
        Mockito.when(typeVar2.getName()).thenReturn("bool");
        Mockito.when(typeVar2.isArray()).thenReturn(false);

        Symbol var1Stub = Mockito.mock(Symbol.class);
        Mockito.when(var1Stub.getType()).thenReturn(typeVar1);
        Mockito.when(var1Stub.getName()).thenReturn("var1");

        Symbol var2Stub = Mockito.mock(Symbol.class);
        Mockito.when(var2Stub.getType()).thenReturn(typeVar2);
        Mockito.when(var2Stub.getName()).thenReturn("var2");

        JmmMethodSignature jmmms = new JmmMethodSignature(new Type("void", false), Arrays.asList(var1Stub, var2Stub));
        assertEquals(jmmms.getParameters().size(), 2);
    }

    @Test
    public void testGetParametersContent() {
        Type typeVar1 = Mockito.mock(Type.class);
        Mockito.when(typeVar1.getName()).thenReturn("int");
        Mockito.when(typeVar1.isArray()).thenReturn(false);

        Type typeVar2 = Mockito.mock(Type.class);
        Mockito.when(typeVar2.getName()).thenReturn("bool");
        Mockito.when(typeVar2.isArray()).thenReturn(false);

        Symbol var1Stub = Mockito.mock(Symbol.class);
        Mockito.when(var1Stub.getType()).thenReturn(typeVar1);
        Mockito.when(var1Stub.getName()).thenReturn("var1");

        Symbol var2Stub = Mockito.mock(Symbol.class);
        Mockito.when(var2Stub.getType()).thenReturn(typeVar2);
        Mockito.when(var2Stub.getName()).thenReturn("var2");

        JmmMethodSignature jmmms = new JmmMethodSignature(new Type("void", false), Arrays.asList(var1Stub, var2Stub));

        List<Symbol> parameters = jmmms.getParameters();
        Symbol var1 = parameters.get(0);
        Symbol var2 = parameters.get(1);

        assertEquals(var1.getType().getName(), "int");
        assertEquals(var2.getType().getName(), "bool");
        assertEquals(var1.getName(), "var1");
        assertEquals(var2.getName(), "var2");
    }

    @Test
    public void testDoParametersMatch() {
        Type typeVar1 = Mockito.mock(Type.class);
        Mockito.when(typeVar1.getName()).thenReturn("int");
        Mockito.when(typeVar1.isArray()).thenReturn(false);

        Type typeVar2 = Mockito.mock(Type.class);
        Mockito.when(typeVar2.getName()).thenReturn("bool");
        Mockito.when(typeVar2.isArray()).thenReturn(false);

        Symbol var1Stub = Mockito.mock(Symbol.class);
        Mockito.when(var1Stub.getType()).thenReturn(typeVar1);
        Mockito.when(var1Stub.getName()).thenReturn("var1");

        Symbol var2Stub = Mockito.mock(Symbol.class);
        Mockito.when(var2Stub.getType()).thenReturn(typeVar2);
        Mockito.when(var2Stub.getName()).thenReturn("var2");

        Symbol var3Stub = Mockito.mock(Symbol.class);
        Mockito.when(var3Stub.getType()).thenReturn(typeVar1);
        Mockito.when(var3Stub.getName()).thenReturn("var3");

        Symbol var4Stub = Mockito.mock(Symbol.class);
        Mockito.when(var4Stub.getType()).thenReturn(typeVar2);
        Mockito.when(var4Stub.getName()).thenReturn("var4");

        JmmMethodSignature jmmms = new JmmMethodSignature(new Type("void", false), Arrays.asList(var1Stub, var2Stub));
        assertTrue(jmmms.doParametersMatch(Arrays.asList(var3Stub, var4Stub)));
    }

    @Test
    public void testIsSameReturnType() {
        Type typeVar1 = Mockito.mock(Type.class);
        Mockito.when(typeVar1.getName()).thenReturn("int");
        Mockito.when(typeVar1.isArray()).thenReturn(false);

        Symbol var1Stub = Mockito.mock(Symbol.class);
        Mockito.when(var1Stub.getType()).thenReturn(typeVar1);
        Mockito.when(var1Stub.getName()).thenReturn("var1");

        JmmMethodSignature jmmms = new JmmMethodSignature(typeVar1, List.of());

        assertTrue(jmmms.isSameReturnType(var1Stub));
    }
}
