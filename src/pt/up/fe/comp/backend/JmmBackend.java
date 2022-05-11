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

    private String getMethodsDefinitions(ClassUnit ollirClass) {
        StringBuilder methodDefinitions = new StringBuilder();

        for(Method method: ollirClass.getMethods()) {
            if (method.isConstructMethod()) {
                methodDefinitions.append(MethodDefinitionGenerator.getConstructorDefinition(method, ollirClass.getSuperClass()));
            } else {
                methodDefinitions.append(MethodDefinitionGenerator.getMethodDefinition(method));
            }

            methodDefinitions.append("\n");
        }

        return methodDefinitions.toString();
    }
}
