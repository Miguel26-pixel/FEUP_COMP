package pt.up.fe.comp.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

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

    public static String invokespecial(String className, String methodName) {
        return "invokespecial(" + className + ", " + "\"" + methodName + "\").V;";
    }

    public static String defaultConstructor(String className) {
        return ".construct " + className + "().V {\n"
                + invokespecial("this", "<init>") + "\n}\n";
    }

}
