package pt.up.fe.comp.semantic.visitors.symbolTableLookup;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;
import pt.up.fe.comp.semantic.visitors.ReportCollectorJmmNodeVisitor;

public class ImportCheckVisitor extends ReportCollectorJmmNodeVisitor<Type, Type> {

    JmmSymbolTable symbolTable;

    public ImportCheckVisitor(JmmSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        addVisit("Identifier",this::visitIdentifier);
        setDefaultVisit(this::visitDefault);
    }

    private Type visitDefault(JmmNode node, Type dummy) {
        for (var child : node.getChildren()) {
            visit(child, dummy);
        }
        return null;
    }

    private Type visitIdentifier(JmmNode node, Type dummy) {
        if (!node.getJmmParent().getKind().equals("VarDeclaration")) { return null; }
        JmmNode varDecl = node.getJmmParent();
        String className = varDecl.getChildren().get(0).get("name");
        if (!className.equals("int") && !className.equals("boolean") && !className.equals("String") &&
                !className.equals(symbolTable.getClassName()) && !className.equals(symbolTable.getSuper())) {
            for (var imp: symbolTable.getImports()) {
                String classImported = imp.substring(imp.lastIndexOf('.') + 1);
                if (classImported.equals(className)) {
                    return new Type(classImported, false);
                }
            }
            addSemanticErrorReport(node, "Class " + className + " does not exist");
        }
        return new Type("", false);
    }
}
