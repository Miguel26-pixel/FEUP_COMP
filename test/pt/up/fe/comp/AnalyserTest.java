package pt.up.fe.comp;

import org.junit.Test;
import org.mockito.Mockito;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

import static org.junit.Assert.*;

public class AnalyserTest {
    @Test
    public void testImport() {
        JmmSemanticsResult result = TestUtils.analyse("import a; import b; import c.d; class dummy {}");

        List<String> imports = result.getSymbolTable().getImports();

        assertTrue(imports.contains("a"));
        assertTrue(imports.contains("b"));
        assertTrue(imports.contains("c.d"));
    }

    @Test
    public void testMethodDeclaration() {
        JmmSemanticsResult result = TestUtils.analyse("class test{public Integer f1(int p1){int integerVar = 0; String stringVar; bool boolVar; int[] intArrayVar;return 0;}}");

        assertTrue(result.getSymbolTable().getMethods().contains("f1"));

        assertEquals(result.getSymbolTable().getReturnType("f1").getName(), "Integer");
        assertFalse(result.getSymbolTable().getReturnType("f1").isArray());

        assertEquals(result.getSymbolTable().getParameters("f1").size(), 1);

        assertEquals(result.getSymbolTable().getParameters("f1").get(0).getName(), "p1");
        assertEquals(result.getSymbolTable().getParameters("f1").get(0).getType().getName(), "int");
        assertFalse(result.getSymbolTable().getParameters("f1").get(0).getType().isArray());
    }

    @Test
    public void testMainMethodDeclaration() {
        JmmSemanticsResult result = TestUtils.analyse("class test{public static void main(String[] args){}}");

        assertTrue(result.getSymbolTable().getMethods().contains("main"));

        assertEquals(result.getSymbolTable().getReturnType("main").getName(), "void");
        assertFalse(result.getSymbolTable().getReturnType("main").isArray());

        assertEquals(result.getSymbolTable().getParameters("main").size(), 1);

        assertEquals(result.getSymbolTable().getParameters("main").get(0).getName(), "args");
        assertEquals(result.getSymbolTable().getParameters("main").get(0).getType().getName(), "String");
        assertTrue(result.getSymbolTable().getParameters("main").get(0).getType().isArray());
    }

    @Test
    public void testMixedMethodDeclaration() {
        JmmSemanticsResult result = TestUtils.analyse("class test{public static void main(String[] args){} public String f2(bool p1){return true;}}");

        assertTrue(result.getSymbolTable().getMethods().contains("main"));

        assertEquals(result.getSymbolTable().getReturnType("main").getName(), "void");
        assertFalse(result.getSymbolTable().getReturnType("main").isArray());

        assertEquals(result.getSymbolTable().getParameters("main").size(), 1);

        assertEquals(result.getSymbolTable().getParameters("main").get(0).getName(), "args");
        assertEquals(result.getSymbolTable().getParameters("main").get(0).getType().getName(), "String");
        assertTrue(result.getSymbolTable().getParameters("main").get(0).getType().isArray());




        assertTrue(result.getSymbolTable().getMethods().contains("f2"));

        assertEquals(result.getSymbolTable().getReturnType("f2").getName(), "String");
        assertFalse(result.getSymbolTable().getReturnType("f2").isArray());

        assertEquals(result.getSymbolTable().getParameters("f2").size(), 1);

        assertEquals(result.getSymbolTable().getParameters("f2").get(0).getName(), "p1");
        assertEquals(result.getSymbolTable().getParameters("f2").get(0).getType().getName(), "bool");
        assertFalse(result.getSymbolTable().getParameters("f2").get(0).getType().isArray());
    }
}
