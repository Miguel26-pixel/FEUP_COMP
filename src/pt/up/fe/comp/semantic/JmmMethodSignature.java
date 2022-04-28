package pt.up.fe.comp.semantic;

import java.util.ArrayList;
import java.util.List;

public class JmmMethodSignature {
    private final String returnType;
    private final List<JmmVariable> parameters;

    public JmmMethodSignature(String returnType, List<JmmVariable> parameters) {
        this.returnType = returnType;
        this.parameters = new ArrayList<>(parameters);
    }

    public String getReturnType() {
        return returnType;
    }

    public List<JmmVariable> getParameters() {
        return parameters;
    }

    public Boolean doParametersMatch(List<JmmVariable> parameters) {
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

    public Boolean isSameReturnType(JmmVariable var) {
        return this.returnType.equals(var.getType());
    }
}
