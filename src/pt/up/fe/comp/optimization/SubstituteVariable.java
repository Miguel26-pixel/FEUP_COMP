package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.analysis.table.Type;

public class SubstituteVariable {
    private String variableName;
    private Type variableType;

    public String getVariableName() {
        return variableName;
    }

    public SubstituteVariable withVariableName(String variable) {
        this.variableName = variable;
        return this;
    }

    public Type getVariableType() {
        return variableType;
    }

    public SubstituteVariable withVariableType(Type variableType) {
        this.variableType = variableType;
        return this;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setVariableType(Type variableType) {
        this.variableType = variableType;
    }
}
