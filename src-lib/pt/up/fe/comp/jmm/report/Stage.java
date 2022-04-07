package pt.up.fe.comp.jmm.report;

import pt.up.fe.specs.util.SpecsStrings;

public enum Stage {
    LEXICAL,
    SYNTATIC,
    SEMANTIC,
    LLIR("LLIR"),
    OPTIMIZATION,
    GENERATION,
    OTHER("Unknown"); // e.g. while parsing inputs, uncaught error, etc.

    private final String name;

    Stage(String name) {
        this.name = name;
    }

    Stage() {
        this.name = SpecsStrings.toCamelCase(name(), "_", false);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
