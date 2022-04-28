package pt.up.fe.comp.semantic;

public class JmmVariable {
    private final String type;
    private final String name;

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
