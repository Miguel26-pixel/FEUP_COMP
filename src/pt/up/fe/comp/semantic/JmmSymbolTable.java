package pt.up.fe.comp.semantic;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.semantic.tables.ImportsTable;
import pt.up.fe.comp.semantic.tables.MethodsTable;
import pt.up.fe.comp.semantic.types.JmmClassSignature;
import pt.up.fe.comp.semantic.visitors.ClassDeclarationVisitor;

import java.util.ArrayList;
import java.util.List;

public class JmmSymbolTable implements SymbolTable {
    private final MethodsTable methods;
    private final ImportsTable imports;
    private final JmmClassSignature classSignature = new JmmClassSignature();

    public JmmSymbolTable(JmmParserResult parserResult) {
        imports = new ImportsTable(parserResult);
        methods = new MethodsTable(parserResult);
        new ClassDeclarationVisitor().visit(parserResult.getRootNode(), classSignature);
    }

    @Override
    public List<String> getImports() {
        return this.imports.getImports();
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
        return new ArrayList<>(this.methods.getMethodSignatures().keySet());
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return this.methods.getReturnType(methodSignature);
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return this.methods.getParameters(methodSignature);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return this.methods.getLocalVariables(methodSignature);
    }
}
