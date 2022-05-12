package pt.up.fe.comp.backend;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;


import java.util.Collections;

public class JmmBackend implements JasminBackend {
    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        StringBuilder jasminCode = new StringBuilder();

        ClassUnit ollirClass = ollirResult.getOllirClass();
        jasminCode.append(getClassDirective(ollirClass)).append("\n");
        jasminCode.append(getSuperDirective(ollirClass)).append("\n");
        jasminCode.append("\n");

        jasminCode.append(getMethodsDefinitions(ollirClass));

        return new JasminResult(ollirResult, jasminCode.toString(), Collections.emptyList());
    }

    private String getClassDirective(ClassUnit ollirClass) {
        StringBuilder classDirective = new StringBuilder(".class ");

        if (ollirClass.isFinalClass()) {
            classDirective.append("final ");
        }

        if (ollirClass.isStaticClass()) {
            classDirective.append("static ");
        }

        if (ollirClass.getClassAccessModifier().toString().equals("DEFAULT")) {
            classDirective.append("public ");
        } else {
            classDirective.append(ollirClass.getClassAccessModifier().toString()).append(" ");
        }

        if (ollirClass.getPackage() != null) {
            classDirective.append(ollirClass.getPackage()).append("/");
        }

        classDirective.append(ollirClass.getClassName());

        return classDirective.toString();
    }

    private String getSuperDirective(ClassUnit ollirClass) {
        return ollirClass.getSuperClass() != null ? ".super " + ollirClass.getSuperClass() : ".super java/lang/Object";
    }

    private String getFieldDefinitions(ClassUnit ollirClass) {
        StringBuilder fieldDefinitions = new StringBuilder();

        for (Field field: ollirClass.getFields()) {
            if (field.getFieldAccessModifier().toString().equals("DEFAULT")) {
                fieldDefinitions.append("public ");
            } else {
                fieldDefinitions.append(field.getFieldAccessModifier().toString()).append(" ");
            }

            if (field.isFinalField()) {
                fieldDefinitions.append("final ");
            }

            if (field.isStaticField()) {
                fieldDefinitions.append("static ");
            }

            //fieldDefinitions.append()
        }

        return fieldDefinitions.toString();
    }

    private String getMethodsDefinitions(ClassUnit ollirClass) {
        StringBuilder methodDefinitions = new StringBuilder();
        MethodDefinitionGenerator mdg = new MethodDefinitionGenerator();

        for(Method method: ollirClass.getMethods()) {
            mdg.setMethod(method);

            if (method.isConstructMethod()) {
                methodDefinitions.append(mdg.getConstructorDefinition(ollirClass.getSuperClass()));
            } else {
                methodDefinitions.append(mdg.getMethodDefinition());
            }

            methodDefinitions.append("\n");
        }

        return methodDefinitions.toString();
    }

    public static String translateType(ClassUnit ollirClass, Type type) {
        ElementType elementType = type.getTypeOfElement();

        switch (elementType) {
            case ARRAYREF:
                return "[" + translateType(((ArrayType) type).getTypeOfElements());
            case OBJECTREF:
            case CLASS:
                return "L" + getFullClassName(ollirClass, ((ClassType) type).getName()) + ";";
            default:
                return translateType(elementType);
        }
    }

    private static String translateType(ElementType elementType) {
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

    private static String getFullClassName(ClassUnit ollirClass, String className) {
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
}
