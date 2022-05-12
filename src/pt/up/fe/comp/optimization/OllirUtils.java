package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.List;

public class OllirUtils {
    public static String getOllirType(Type type) {
        String ollirType = type.isArray() ? "array." : "";
        if (type.getName().equals("boolean")) {
            return ollirType + "bool";
        }
        if (type.getName().equals("int")) {
            return ollirType + "i32";
        }
        if (type.getName().equals("void")) {
            return ollirType + "V";
        }
        return ollirType + type.getName();
    }

    public static String getOllirType(Symbol symbol) {
        return getOllirType(symbol.getType());
    }

    private static String invoke(String invokeFunction, String className, String methodName, List<String> arguments) {
        StringBuilder stringBuilder = new StringBuilder(invokeFunction + "(" + className + ", " + "\"" + methodName + "\"");
        if (!arguments.isEmpty()) {
            for (var arg : arguments) {
                stringBuilder.append(arg).append(", ");
            }
            stringBuilder.delete(stringBuilder.lastIndexOf(","), stringBuilder.length());
        }
        stringBuilder.append(").V");
        return stringBuilder.toString();
    }

    public static String invokespecial(String className, String methodName, List<String> arguments) {
        return invoke("invokespecial", className, methodName, arguments);
    }

    public static String invokestatic(String className, String methodName, List<String> arguments) {
        return invoke("invokestatic", className, methodName, arguments);
    }

    public static String putField(String className, String variableName, String value) {
        return "putfield(" + className + ", " + variableName + ", " + value + ").V";
    }

    public static String getField(String className, String field, String ollirType) {
        return "getfield(" + className + ", " + field + "." + ollirType + ")." + ollirType;
    }

    public static String arrayLength(String variableName, String elementOllirType) {
        return "arraylength(" + variableName + ".array." + elementOllirType + ")." + elementOllirType;
    }

}
