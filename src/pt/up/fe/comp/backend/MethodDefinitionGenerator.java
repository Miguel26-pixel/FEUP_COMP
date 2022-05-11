package pt.up.fe.comp.backend;

import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Type;

import java.nio.charset.StandardCharsets;

public class MethodDefinitionGenerator {
    public static String getConstructorDefinition(Method method, String superName) {
        StringBuilder constructorDefinition = new StringBuilder();

        constructorDefinition.append(getConstructorHeader(method));

        constructorDefinition.append("\taload_0\n");

        if (superName == null) {
            superName = "java/lang/Object";
        }

        constructorDefinition.append("\tinvokenonvirtual ").append(superName).append("/<init>()V\n");
        constructorDefinition.append("\treturn\n");
        constructorDefinition.append(".end method\n");

        return constructorDefinition.toString();
    }

    public static String getMethodDefinition(Method method) {
        StringBuilder methodDefinition = new StringBuilder();

        methodDefinition.append(getMethodHeader(method));

        return methodDefinition.toString();
    }

    private static String getConstructorHeader(Method method) {
        StringBuilder constructorHeader = new StringBuilder(".method ");

        if (method.getMethodAccessModifier().toString().equals("DEFAULT")) {
            constructorHeader.append("public ");
        } else {
            constructorHeader.append(method.getMethodAccessModifier().toString().toLowerCase()).append(" ");
        }

        constructorHeader.append("<init>()V\n");

        return constructorHeader.toString();
    }

    private static String getMethodHeader(Method method) {
        StringBuilder methodHeader = new StringBuilder(".method ");

        if (method.isFinalMethod()) {
            methodHeader.append("final ");
        }

        if (method.isStaticMethod()) {
            methodHeader.append("static ");
        }

        if (method.getMethodAccessModifier().toString().equals("DEFAULT")) {
            methodHeader.append("public ");
        } else {
            methodHeader.append(method.getMethodAccessModifier().toString().toLowerCase()).append(" ");
        }

        methodHeader.append(method.getMethodName()).append("(");

        getParameters(method);

        return methodHeader.toString();
    }

    private static String getParameters(Method method) {
        StringBuilder methodParameters = new StringBuilder();
        method.show();

        for (Element parameter: method.getParams()) {
            parameter.show();
        }

        return methodParameters.toString();
    }
}
