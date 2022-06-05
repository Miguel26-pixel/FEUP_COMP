package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;

import static pt.up.fe.comp.optimization.OllirUtils.getOllirType;

public class SubstituteVariable {
    private Type variableType;
    private String variableName = "";
    private String variableValue;
    private Type assignType = new Type("void", false);

    public SubstituteVariable(String variableName) {
        this.variableName = variableName;
    }

    public Type getVariableType() {
        return variableType;
    }

    public void setVariableType(Type variableType) {
        this.variableType = variableType;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setVariableTypeIfNotPresent(Type variableType) {
        if (this.variableType == null) {
            this.variableType = variableType;
        }
    }

    public String getValue() {
        return variableValue != null ? variableValue : variableName;
    }

    public void setValue(String variableValue) {
        this.variableValue = variableValue;
    }

    public String getSubstituteWithType() {
        String ollirType = variableType != null ? getOllirType(variableType) : "i32";
        return getSubstitute() + "." + ollirType;
    }

    public String getSubstitute() {
        return getValue() != null ? getValue() : getVariableName();
    }

    public String getInvokeString(JmmNode node, JmmSymbolTable symbolTable) {
        String invokeClass = getSubstitute();
        return symbolTable.isLocalVariable(node, invokeClass)
                ? invokeClass + "." + getOllirType(getVariableType()) : invokeClass;
    }
}
