package pt.up.fe.comp.backend;

import org.specs.comp.ollir.*;

public class MethodDefinitionGenerator {
    private Method method;

    public String getConstructorDefinition(String superName) {
        StringBuilder constructorDefinition = new StringBuilder(".method ");

        if (method.getMethodAccessModifier().toString().equals("DEFAULT")) {
            constructorDefinition.append("public ");
        } else {
            constructorDefinition.append(method.getMethodAccessModifier().toString().toLowerCase()).append(" ");
        }

        constructorDefinition.append("<init>()V\n");

        constructorDefinition.append("\taload_0\n");

        if (superName == null) {
            superName = "java/lang/Object";
        }

        constructorDefinition.append("\tinvokenonvirtual ").append(superName).append("/<init>()V\n");
        constructorDefinition.append("\treturn\n");
        constructorDefinition.append(".end method\n");

        return constructorDefinition.toString();
    }

    public String getMethodDefinition() {
        StringBuilder methodDefinition = new StringBuilder();

        methodDefinition.append(getMethodHeader()).append("\n");

        methodDefinition.append(".end method\n");

        return methodDefinition.toString();
    }

    private String getMethodHeader() {
        StringBuilder methodHeader = new StringBuilder(".method ");

        if (method.getMethodAccessModifier().toString().equals("DEFAULT")) {
            methodHeader.append("public ");
        } else {
            methodHeader.append(method.getMethodAccessModifier().toString().toLowerCase()).append(" ");
        }

        if (method.isFinalMethod()) {
            methodHeader.append("final ");
        }

        if (method.isStaticMethod()) {
            methodHeader.append("static ");
        }

        methodHeader.append(method.getMethodName()).append("(");
        methodHeader.append(getParameters()).append(")");

        return methodHeader.toString();
    }

    private String getParameters() {
        StringBuilder methodParameters = new StringBuilder();

        for (Element parameter: method.getParams()) {
            methodParameters.append(JmmBackend.translateType(method.getOllirClass(), parameter.getType()));
        }

        return methodParameters.toString();
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
