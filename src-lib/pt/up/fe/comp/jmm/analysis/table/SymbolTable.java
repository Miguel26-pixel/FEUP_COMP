package pt.up.fe.comp.jmm.analysis.table;

import java.util.List;
import java.util.stream.Collectors;

public interface SymbolTable {

    /**
     * @return a list of fully qualified names of imports
     */
    List<String> getImports();

    /**
     * @return the name of the main class
     */
    String getClassName();

    /**
     * 
     * @return the name that the classes extends, or null if the class does not extend another class
     */
    String getSuper();

    /**
     * 
     * @return a list of Symbols that represent the fields of the class
     */
    List<Symbol> getFields();

    /**
     * 
     * @return a list with the methods signatures of the class
     */
    List<String> getMethods();

    /**
     * 
     * @return the return type of the given method
     */
    Type getReturnType(String methodSignature);

    /**
     * 
     * @param methodSignature
     * @return a list of parameters of the given method
     */
    List<Symbol> getParameters(String methodSignature);

    /**
     * 
     * @param methodSignature
     * @return a list of local variables declared in the given method
     */
    List<Symbol> getLocalVariables(String methodSignature);

    /**
     * 
     * @return a String with information about the contents of the SymbolTable
     */
    default String print() {
        var builder = new StringBuilder();

        builder.append("Class: " + getClassName() + "\n");
        var superClass = getSuper() != null ? getSuper() : "java.lang.Object";
        builder.append("Super: " + superClass + "\n");
        builder.append("\nImports:");
        var imports = getImports();

        if (imports.isEmpty()) {
            builder.append(" <no imports>\n");
        } else {
            builder.append("\n");
            imports.forEach(fullImport -> builder.append(" - " + fullImport + "\n"));
        }

        var fields = getFields();
        builder.append("\nFields:");
        if (fields.isEmpty()) {
            builder.append(" <no fields>\n");
        } else {
            builder.append("\n");
            fields.forEach(field -> builder.append(" - " + field.print() + "\n"));
        }

        var methods = getMethods();
        builder.append("\nMethods: " + methods.size() + "\n");

        for (var method : methods) {
            builder.append(" - signature: ").append(method);
            builder.append("; returnType: ").append(getReturnType(method).print());

            // var returnType = getReturnType(method);
            var params = getParameters(method);
            builder.append("; params: ");

            if (params.isEmpty()) {
                builder.append(" <no params>");
            } else {
                var paramsString = params.stream().map(param -> param != null ? param.print() : "<null param>")
                        .collect(Collectors.joining(", "));
                builder.append(paramsString);
            }

            /**
             * Print of local variables with contribution from group comp2022-2c
             */
            var localVariables = getLocalVariables(method);
            builder.append("; local vars: ");

            if (localVariables.isEmpty()) {
                builder.append("<no vars>");
            } else {
                var localVarsString = localVariables.stream()
                        .map(localVar -> localVar != null ? localVar.print() : "<null var>")
                        .collect(Collectors.joining(", "));
                builder.append(localVarsString);
            }

            builder.append("\n");
        }

        return builder.toString();
    }

}
