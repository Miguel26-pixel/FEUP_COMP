package pt.up.fe.comp.backend;

import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Type;

public class MethodDefinitionGenerator {
    public static String getConstructorDefinition(Method method, String superName) {
        String constructorDefinition = ".method ";

        if (method.getMethodAccessModifier().toString().equals("DEFAULT")) {
            constructorDefinition += "public ";
        } else {
            constructorDefinition += method.getMethodAccessModifier().toString().toLowerCase() + " ";
        }

        constructorDefinition += "<init>()V\n";

        constructorDefinition += "\taload_0\n";

        if (superName == null) {
            superName = "java/lang/Object";
        }

        constructorDefinition += "\tinvokenonvirtual " + superName + "/<init>()V\n";
        constructorDefinition += "\treturn\n";
        constructorDefinition += ".end method\n";

        return constructorDefinition;
    }

    public static String getMethodDefinition(Method method) {
        String methodDefinition = "";

        methodDefinition += getMethodHeader(method);

        return methodDefinition;
    }

    private static String getMethodHeader(Method method) {
        String methodHeader = ".method ";

        if (method.isFinalMethod()) {
            methodHeader += "final ";
        }

        if (method.isStaticMethod()) {
            methodHeader += "static ";
        }

        if (method.getMethodAccessModifier().toString().equals("DEFAULT")) {
            methodHeader += "public ";
        } else {
            methodHeader += method.getMethodAccessModifier().toString().toLowerCase() + " ";
        }

        methodHeader += method.getMethodName() + "(";

        return methodHeader;
    }
}
