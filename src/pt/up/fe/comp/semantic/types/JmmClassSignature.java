package pt.up.fe.comp.semantic.types;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.ArrayList;
import java.util.List;

public class JmmClassSignature {
    String className;
    String superName;
    List<Symbol> fields = new ArrayList<>();

    public String getClassName() {
        return className;
    }

    public String getSuperName() {
        return superName;
    }

    public List<Symbol> getFields() {
        return fields;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Class " + this.className);
        if (this.superName != null) {
            str.append(" extends ").append(this.superName);
        }
        str.append("\n");
        for (Symbol field: this.fields){
            str.append(field.toString()).append("\n");
        }
        return str.toString();
    }
}
