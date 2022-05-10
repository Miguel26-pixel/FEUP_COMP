package pt.up.fe.comp.backend;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;


import java.util.Collections;

public class JmmBackend implements JasminBackend {
    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        String jasminCode = "";

        ClassUnit ollirClass = ollirResult.getOllirClass();
        jasminCode += getClassDirective(ollirClass) + "\n";
        jasminCode += getSuperDirective(ollirClass) + "\n";
        jasminCode += "\n";

        jasminCode += getMethodsDefinitions(ollirClass);

        return new JasminResult(ollirResult, jasminCode, Collections.emptyList());
    }

    private String getClassDirective(ClassUnit ollirClass) {
        String classDirective = ".class ";

        if (ollirClass.isFinalClass()) {
            classDirective += "final ";
        }

        if (ollirClass.isStaticClass()) {
            classDirective += "static ";
        }

        if (ollirClass.getClassAccessModifier().toString().equals("DEFAULT")) {
            classDirective += "public ";
        } else {
            classDirective += ollirClass.getClassAccessModifier().toString() + " ";
        }

        if (ollirClass.getPackage() != null) {
            classDirective += ollirClass.getPackage() + "/";
        }

        classDirective += ollirClass.getClassName();

        return classDirective;
    }

    private String getSuperDirective(ClassUnit ollirClass) {
        return ollirClass.getSuperClass() != null ? ".super " + ollirClass.getSuperClass() : ".super java/lang/Object";
    }

    private String getMethodsDefinitions(ClassUnit ollirClass) {
        String methodDefinitions = "";

        for(Method method: ollirClass.getMethods()) {
            if (method.isConstructMethod()) {
                methodDefinitions += MethodDefinitionGenerator.getConstructorDefinition(method, ollirClass.getSuperClass());
            } else {
                methodDefinitions += MethodDefinitionGenerator.getMethodDefinition(method);
            }

            methodDefinitions += "\n";
        }

        return methodDefinitions;
    }
}
