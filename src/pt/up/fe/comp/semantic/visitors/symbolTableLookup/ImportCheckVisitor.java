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
        if (!className.equals("int") && !className.equals("bool") &&
                !className.equals("String") && !className.equals(symbolTable.getClassName())) {
            for (var imp: symbolTable.getImports()) {
                String classImported = imp.substring(imp.lastIndexOf('.') + 1);
                if (classImported.equals(className)) {
                    return new Type(classImported, false);
                }
            }
            addSemanticErrorReport(node, "Class " + className + " does not exists");
        }
        return new Type("", false);
    }

    /*private Type visitArguments(JmmNode node, Type dummy) {
        Optional<JmmNode> methodCall = node.getAncestor("MethodCall");
        List<JmmNode> args = node.getChildren();
        if (methodCall.isPresent()) {
            List<Symbol> params = symbolTable.getParameters(methodCall.get().getChildren().get(0).get("name"));
            if (params.size() != args.size()) {
                addSemanticErrorReport(node, "Invalid number of arguments");
            } else {
                for (int i = 0; i < args.size(); i++) {

                }
            }
        } else {
            addSemanticErrorReport(node, "Invalid arguments");
        }
        return new Type("", false);
    }*/
}
