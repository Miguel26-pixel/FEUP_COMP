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
            methodParameters.append(translateType(parameter.getType()));
        }

        return methodParameters.toString();
    }

    private String translateType(Type type) {
        ElementType elementType = type.getTypeOfElement();

        switch (elementType) {
            case ARRAYREF:
                return "[" + translateType(((ArrayType) type).getTypeOfElements());
            case OBJECTREF:
            case CLASS:
                return "L" + getFullClassName(((ClassType) type).getName()) + ";";
            default:
                return translateType(elementType);
        }
    }

    private String translateType(ElementType elementType) {
        switch (elementType) {
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case STRING:
                return "Ljava/lang/String;";
            case THIS:
                return "this";
            case VOID:
                return "V";
            default:
                return "";
        }
    }

    private String getFullClassName(String className) {
        ClassUnit ollirClass = method.getOllirClass();

        if (ollirClass.isImportedClass(className)) {
            for(String fullImport: ollirClass.getImports()) {
                int lastSeparatorIndex = className.lastIndexOf(".");

                if (lastSeparatorIndex < 0 && fullImport.equals(className)) {
                    return className;
                } else if (fullImport.substring(lastSeparatorIndex + 1).equals(className)) {
                    return fullImport;
                }
            }
        }

        return className;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
