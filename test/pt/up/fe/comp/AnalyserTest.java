package pt.up.fe.comp;

import org.junit.Test;
import org.mockito.Mockito;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.semantic.JmmSymbolTable;

import java.util.List;

import static org.junit.Assert.*;

public class AnalyserTest {
    @Test
    public void testImport() {
        JmmSemanticsResult result = TestUtils.analyse("import a; import b; import c.d; class dummy extends other {}");

        List<String> imports = result.getSymbolTable().getImports();

        assertTrue(imports.contains("a"));
        assertTrue(imports.contains("b"));
        assertTrue(imports.contains("c.d"));
    }

    @Test
    public void testMethodDeclaration() {
        JmmSemanticsResult result = TestUtils.analyse("class test{public Integer f1(int p1){return 0;}}");

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

    @Test
    public void testClassInfo() {
        JmmSemanticsResult result = TestUtils.analyse("class test extends other{int a; String str; int[] b; public int foo(){return 0;}}");
        SymbolTable symbolTable = result.getSymbolTable();
        assertEquals(symbolTable.getClassName(), "test");
        assertEquals(symbolTable.getSuper(), "other");
        assertEquals(symbolTable.getFields().size(), 3);
        assertEquals(symbolTable.getFields().get(0), new Symbol(new Type("int",false), "a"));
        assertEquals(symbolTable.getFields().get(1), new Symbol(new Type("String",false), "str"));
        assertEquals(symbolTable.getFields().get(2), new Symbol(new Type("int",true), "b"));
    }

    @Test
    public void testClassInfoSimple() {
        JmmSemanticsResult result = TestUtils.analyse("class test {public int foo(){return 0;}}");
        SymbolTable symbolTable = result.getSymbolTable();
        assertEquals(symbolTable.getClassName(), "test");
        assertNull(symbolTable.getSuper());
        assertEquals(symbolTable.getFields().size(), 0);
    }
}
