package pt.up.fe.comp.semantic;

public class JmmVariable {
    public final String type;
    public final String name;

    public JmmVariable(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
