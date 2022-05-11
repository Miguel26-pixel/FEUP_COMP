package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.analysis.table.Type;

public class SubstituteVariable {
    private String variableName;
    private Type variableType;
    private String value;

    public SubstituteVariable(String name){
        this.variableName = name;
    }

    public String getVariableName() {
        return variableName;
    }

    public Type getVariableType() {
        return variableType;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setVariableType(Type variableType) {
        this.variableType = variableType;
    }

    public void setValue(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    public boolean isValueSet(){
        return this.value != null;
    }
}
