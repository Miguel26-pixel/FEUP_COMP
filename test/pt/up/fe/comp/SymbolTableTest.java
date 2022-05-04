package pt.up.fe.comp;

import org.junit.Test;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;

import java.util.List;

import static org.junit.Assert.*;

public class SymbolTableTest {
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

        assertTrue(result.getSymbolTable().getMethodsTable().contains("f1"));

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

        assertTrue(result.getSymbolTable().getMethodsTable().contains("main"));

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

        assertTrue(result.getSymbolTable().getMethodsTable().contains("main"));

        assertEquals(result.getSymbolTable().getReturnType("main").getName(), "void");
        assertFalse(result.getSymbolTable().getReturnType("main").isArray());

        assertEquals(result.getSymbolTable().getParameters("main").size(), 1);

        assertEquals(result.getSymbolTable().getParameters("main").get(0).getName(), "args");
        assertEquals(result.getSymbolTable().getParameters("main").get(0).getType().getName(), "String");
        assertTrue(result.getSymbolTable().getParameters("main").get(0).getType().isArray());

        assertTrue(result.getSymbolTable().getMethodsTable().contains("f2"));

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
        assertEquals(symbolTable.getFields().get(0), new Symbol(new Type("int", false), "a"));
        assertEquals(symbolTable.getFields().get(1), new Symbol(new Type("String", false), "str"));
        assertEquals(symbolTable.getFields().get(2), new Symbol(new Type("int", true), "b"));
    }

    @Test
    public void testClassInfoSimple() {
        JmmSemanticsResult result = TestUtils.analyse("class test {public int foo(){return 0;}}");
        SymbolTable symbolTable = result.getSymbolTable();
        assertEquals(symbolTable.getClassName(), "test");
        assertNull(symbolTable.getSuper());
        assertEquals(symbolTable.getFields().size(), 0);
    }

    @Test
    public void testLocalVariables() {
        JmmSemanticsResult result = TestUtils.analyse(
                "class test {" +
                        "public int foo(){int a; String b; int[] c; return 0;}" +
                        "public static void main(String[] args){int c;}" +
                        "}"
        );

        assertEquals(result.getSymbolTable().getLocalVariables("bar").size(), 0);

        var fooLocalVariables = result.getSymbolTable().getLocalVariables("foo");
        var mainLocalVariables = result.getSymbolTable().getLocalVariables("main");

        assertEquals(fooLocalVariables.size(), 3);
        assertTrue(fooLocalVariables.contains(new Symbol(new Type("int", false), "a")));
        assertTrue(fooLocalVariables.contains(new Symbol(new Type("String", false), "b")));
        assertTrue(fooLocalVariables.contains(new Symbol(new Type("int", true), "c")));

        assertEquals(mainLocalVariables.size(), 1);
        assertTrue(mainLocalVariables.contains(new Symbol(new Type("int", false), "c")));
    }

    @Test
    public void testGetClosestSymbolPresentInMethod() {
        JmmSemanticsResult result = TestUtils.analyse(
                "class test {" +
                        "String c;" +
                        "public int foo() {int a; String b; int[] c = new int[3]; c[2] = 5; return 0;}" +
                        "}"
        );
        JmmNode node = findChildIndexation(result.getRootNode(), "c");
        JmmSymbolTable symbolTable = (JmmSymbolTable) result.getSymbolTable();
        var closestSymbol = symbolTable.getClosestSymbol(node, "c");
        assertTrue(closestSymbol.isPresent());
        assertEquals("c", closestSymbol.get().getName());
        assertEquals(closestSymbol.get().getType(), new Type("int", true));
    }

    @Test
    public void testGetClosestSymbolNotPresent1() {
        JmmSemanticsResult result = TestUtils.analyse(
                "class test {" +
                        "String c;" +
                        "public int foo() {int a; String b; d[2] = 5; return 0;}" +
                        "}"
        );
        JmmNode node = findChildIndexation(result.getRootNode(), "d");
        JmmSymbolTable symbolTable = (JmmSymbolTable) result.getSymbolTable();
        var closestSymbol = symbolTable.getClosestSymbol(node, "d");
        assertTrue(closestSymbol.isEmpty());
    }

    @Test
    public void testGetClosestSymbolPresentAsClassField() {
        JmmSemanticsResult result = TestUtils.analyse(
                "class test {" +
                        "int[] c = new int[3];" +
                        "public int foo() {int a; String b; c[3] = 1; return 0;}" +
                        "}"
        );
        JmmNode node = findChildIndexation(result.getRootNode(), "c");
        JmmSymbolTable symbolTable = (JmmSymbolTable) result.getSymbolTable();
        var closestSymbol = symbolTable.getClosestSymbol(node, "c");
        assertTrue(closestSymbol.isPresent());
        assertEquals("c", closestSymbol.get().getName());
        assertEquals(closestSymbol.get().getType(), new Type("int", true));
    }

    private JmmNode findChildIndexation(JmmNode node, String name) {
        if (node.getKind().equals("Indexation")) {
            JmmNode identifier = node.getChildren().get(0);
            if (identifier.get("name").equals(name)) {
                return identifier;
            }
        }
        for (var child : node.getChildren()) {
            var identifier = findChildIndexation(child, name);
            if (identifier != null) {
                return identifier;
            }
        }
        return null;
    }
}
