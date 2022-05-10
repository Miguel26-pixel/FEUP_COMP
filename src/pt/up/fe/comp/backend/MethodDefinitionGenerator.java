package pt.up.fe.comp.backend;

import org.specs.comp.ollir.Method;

public class MethodDefinitionGenerator {
    public static String getConstructorDefinition(Method method, String superName) {
        String constructorDefinition = ".method public <init>()V\n";
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


        return methodDefinition;
    }
}
