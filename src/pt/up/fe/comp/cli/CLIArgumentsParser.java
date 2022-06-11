package pt.up.fe.comp.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CLIArgumentsParser {
    private final Map<String, String> config;
    private static final Pattern registerOptPattern = Pattern.compile("^-r=");
    private static final Pattern inputFileOptPattern = Pattern.compile("^-i=");
    private static final Pattern classNamePattern = Pattern.compile("/?([^./]+)\\.jmm$", Pattern.DOTALL);

    public CLIArgumentsParser(String[] args) throws RuntimeException {
        this.config = new HashMap<>();
        this.parse(args);
    }

    private void parse(String[] args) throws RuntimeException{
        this.config.put("optimize", "false");
        this.config.put("registerAllocation", "-1");
        this.config.put("debug", "false");
        this.config.put("inputFile", "");
        this.config.put("className", "");

        for (String argument: args) {
            if (argument.equals("-o")) {
                this.config.replace("optimize", "true");
            } else if (argument.equals("-d")) {
                this.config.replace("debug", "true");
            }

            Matcher patternMatch = registerOptPattern.matcher(argument);

            if (patternMatch.find()) {
                try {
                    Integer.parseInt(argument.substring(3));
                } catch (Exception e) {
                    throw new RuntimeException("Invalid number of registers.");
                }
                this.config.replace("registerAllocation", argument.substring(3));
            }

            patternMatch = inputFileOptPattern.matcher(argument);

            if (patternMatch.find()) {
                patternMatch = classNamePattern.matcher(argument.substring(3));
                if (patternMatch.find()) {
                    this.config.replace("className", patternMatch.group(1));
                    this.config.replace("inputFile", argument.substring(3));
                }
            }
        }

        if (this.config.get("inputFile").equals("")) {
            throw new RuntimeException("Inexistent or incorrect input file.");
        }
    }

    public Map<String, String> getConfig() {
        return config;
    }
}
