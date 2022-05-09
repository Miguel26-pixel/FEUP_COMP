package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.Objects;

public class OllirUtils {
    private static String getOllirType(Symbol symbol) {
        Type type = symbol.getType();
        if (type.isArray()) {
            return "array";
        }
        if (type.getName().equals("boolean")) {
            return "bool";
        }
        if (type.getName().equals("int")) {
            return "i32";
        }
        return symbol.getName();
    }

    public static String putField(String className, Symbol field, int valuePosition, Symbol value) {
        return "putfield(" + className + ", " + field.getName()
                + "." + getOllirType(field) + ", $" + valuePosition + "." + value.getName() + "." + getOllirType(value) + ").V;";
    }

    public static String invokespecial(String className, String methodName) {
        return "invokespecial(" + className + ", " + "\"" + methodName + "\").V;";
    }

    public static String defaultConstructor(String className) {
        return ".construct " + className + "().V {\n"
                + invokespecial("this", "<init>") + "\n}\n";
    }

}
