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

    public static String putField(String className, Symbol field, int valuePosition, Symbol value) {
        return "putfield(" + className + ", " + field.getName()
                + "." + getOllirType(field) + ", $" + valuePosition + "." + value.getName() + "." + getOllirType(value) + ").V;";
    }

    private static String invoke(String invokeFunction, String className, String methodName, List<String> arguments) {
        StringBuilder stringBuilder = new StringBuilder(invokeFunction + "(" + className + ", " + "\"" + methodName + "\"");
        if (!arguments.isEmpty()) {
            for (var arg : arguments) {
                stringBuilder.append(arg).append(", ");
            }
            stringBuilder.delete(stringBuilder.lastIndexOf(","), stringBuilder.length());
        }
        stringBuilder.append(").V;");
        return stringBuilder.toString();
    }

    public static String invokespecial(String className, String methodName, List<String> arguments) {
        return invoke("invokespecial", className, methodName, arguments);
    }

    public static String invokestatic(String className, String methodName, List<String> arguments) {
        return invoke("invokestatic", className, methodName, arguments);
    }

    public static String defaultConstructor(String className) {
        return ".construct " + className + "().V {\n"
                + invokespecial("this", "<init>", new ArrayList<>()) + "\n}\n";
    }

}
