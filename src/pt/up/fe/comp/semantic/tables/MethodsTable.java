package pt.up.fe.comp.semantic.tables;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.semantic.types.JmmMethodSignature;
import pt.up.fe.comp.semantic.visitors.symbolTableBuilder.LocalVariablesVisitor;
import pt.up.fe.comp.semantic.visitors.symbolTableBuilder.MethodDeclarationVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodsTable extends ReportCollectorTable {
    private final Map<String, JmmMethodSignature> methodSignatures = new HashMap<>();
    private final Map<String, List<Symbol>> localVariables = new HashMap<>();

    public MethodsTable(JmmParserResult parserResult) {
        MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
        LocalVariablesVisitor localVariablesVisitor = new LocalVariablesVisitor();
        methodDeclarationVisitor.visit(parserResult.getRootNode(), this.methodSignatures);
        localVariablesVisitor.visit(parserResult.getRootNode(), this.localVariables);
        this.reports.addAll(methodDeclarationVisitor.getReports());
        this.reports.addAll(localVariablesVisitor.getReports());
        System.out.println(this);
    }

    public Map<String, JmmMethodSignature> getMethodSignatures() {
        return methodSignatures;
    }

    public Type getReturnType(String methodSignature) {
        if (methodSignatures.containsKey(methodSignature)) {
            return methodSignatures.get(methodSignature).getReturnType();
        }
        return new Type("NULL", false);
    }

    public List<Symbol> getParameters(String methodName) {
        if (methodSignatures.containsKey(methodName)) {
            return methodSignatures.get(methodName).getParameters();
        }
        return new ArrayList<>();
    }

    public List<Symbol> getLocalVariables(String methodName) {
        if (this.localVariables.containsKey(methodName)) {
            return this.localVariables.get(methodName);
        }
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, JmmMethodSignature> entry : methodSignatures.entrySet()) {
            str.append("Method ").append(entry.getKey()).append("\n");
            JmmMethodSignature method = entry.getValue();
            str.append("Return Type: ").append(method.getReturnType()).append("\n");
            for (Symbol parameter : method.getParameters()) {
                str.append("Parameter Type: ").append(parameter.getType()).append(", Parameter Name: ").append(parameter.getName()).append("\n");
            }
            for (Symbol localVariable : localVariables.get(entry.getKey())) {
                str.append("Local Variable Type: ").append(localVariable.getType()).append(", Local Variable Name: ").append(localVariable.getName()).append("\n");
            }
            str.append("-----\n");
        }
        return str.toString();
    }
}
