package pt.up.fe.comp.semantic.tables;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.semantic.types.JmmClassSignature;
import pt.up.fe.comp.semantic.visitors.ClassDeclarationVisitor;

import java.util.ArrayList;
import java.util.List;

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
    public List<String> getMethodsTable() {
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
}
