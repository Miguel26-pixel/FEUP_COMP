package pt.up.fe.comp.semantic.tables;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.semantic.types.JmmClassSignature;
import pt.up.fe.comp.semantic.visitors.symbolTableBuilder.ClassDeclarationVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JmmSymbolTable extends ReportCollectorTable implements SymbolTable {
    private final MethodsTable methodsTable;
    private final ImportsTable importsTable;
    private final JmmClassSignature classSignature = new JmmClassSignature();

    public JmmSymbolTable(JmmParserResult parserResult) {
        importsTable = new ImportsTable(parserResult);
        methodsTable = new MethodsTable(parserResult);
        ClassDeclarationVisitor classDeclarationVisitor = new ClassDeclarationVisitor();
        classDeclarationVisitor.visit(parserResult.getRootNode(), classSignature);
        this.reports.addAll(importsTable.getReports());
        this.reports.addAll(methodsTable.getReports());
        this.reports.addAll(classDeclarationVisitor.getReports());
    }

    @Override
    public List<String> getImports() {
        return this.importsTable.getImports();
    }

    @Override
    public String getClassName() {
        return classSignature.getClassName();
    }

    @Override
    public String getSuper() {
        return classSignature.getSuperName();
    }

    @Override
    public List<Symbol> getFields() {
        return classSignature.getFields();
    }

    @Override
    public List<String> getMethods() {
        return new ArrayList<>(this.methodsTable.getMethodSignatures().keySet());
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return this.methodsTable.getReturnType(methodSignature);
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return this.methodsTable.getParameters(methodSignature);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return this.methodsTable.getLocalVariables(methodSignature);
    }

    public Optional<Symbol> getClosestSymbol(JmmNode node, String name) {
        var method = getClosestMethod(node);
        if (method.isPresent()) {
            String methodName = method.get().getKind().equals("RegularMethod") ?
                    method.get().getChildren().get(1).get("name") : "main";
            for (Symbol symbol : methodsTable.getLocalVariables(methodName)) {
                if (symbol.getName().equals(name)) {
                    return Optional.of(symbol);
                }
            }
            for (Symbol symbol : methodsTable.getParameters(methodName)) {
                if (symbol.getName().equals(name)) {
                    return Optional.of(symbol);
                }
            }
        }
        for (Symbol symbol : classSignature.getFields()) {
            if (symbol.getName().equals(name)) {
                return Optional.of(symbol);
            }
        }
        return Optional.empty();
    }

    private Optional<JmmNode> getClosestMethod(JmmNode node) {
        var method = node.getAncestor("RegularMethod");
        if (method.isPresent()) {
            return method;
        }
        method = node.getAncestor("MainMethod");
        return method;
    }
}
