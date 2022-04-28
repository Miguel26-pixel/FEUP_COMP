package pt.up.fe.comp.semantic;

import java.util.ArrayList;
import java.util.List;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class JmmMethodSignature {
    private final Type returnType;
    private final List<Symbol> parameters;

    public JmmMethodSignature(Type returnType, List<Symbol> parameters) {
        this.returnType = returnType;
        this.parameters = new ArrayList<>(parameters);
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Symbol> getParameters() {
        return parameters;
    }

    public Boolean doParametersMatch(List<Symbol> parameters) {
        if (this.parameters.size() != parameters.size()) {
            return false;
        }

        for (int index = 0; index < this.parameters.size(); index++) {
            if (!this.parameters.get(index).getType().equals(parameters.get(index).getType())) {
                return false;
            }
        }

        return true;
    }

    public Boolean isSameReturnType(Symbol var) {
        return this.returnType.equals(var.getType());
    }
}
