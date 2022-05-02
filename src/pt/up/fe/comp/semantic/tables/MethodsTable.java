package pt.up.fe.comp.semantic.tables;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.semantic.types.JmmMethodSignature;
import pt.up.fe.comp.semantic.visitors.LocalVariablesVisitor;
import pt.up.fe.comp.semantic.visitors.MethodDeclarationVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodsTable {
    private final Map<String, JmmMethodSignature> methodSignatures = new HashMap<>();
    private final Map<String, List<Symbol>> localVariables = new HashMap<>();

    public MethodsTable(JmmParserResult parserResult) {
        MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
        LocalVariablesVisitor localVariablesVisitor = new LocalVariablesVisitor();
        methodDeclarationVisitor.visit(parserResult.getRootNode(), this.methodSignatures);
        localVariablesVisitor.visit(parserResult.getRootNode(), this.localVariables);
    }

    public Map<String, JmmMethodSignature> getMethodSignatures() {
        return methodSignatures;
    }

    public Map<String, List<Symbol>> getLocalVariables() {
        return localVariables;
    }

    public Type getReturnType(String methodSignature) {
        if (methodSignatures.containsKey(methodSignature)) {
            return methodSignatures.get(methodSignature).getReturnType();
        }

        return new Type("NULL", false);
    }

    public List<Symbol> getParameters(String methodSignature) {
        if (methodSignatures.containsKey(methodSignature)) {
            return methodSignatures.get(methodSignature).getParameters();
        }
        return new ArrayList<>();
    }

    public List<Symbol> getLocalVariables(String methodSignature) {
        if (this.localVariables.containsKey(methodSignature)) {
            return this.localVariables.get(methodSignature);
        }
        return new ArrayList<>();
    }
}
