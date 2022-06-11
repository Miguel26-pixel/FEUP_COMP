package pt.up.fe.comp.backend;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadOptimizer {
    private final Pattern loadPattern = Pattern.compile("^[ia]load$");
    private final Pattern storePattern = Pattern.compile("^[ia]store$");
    private final String[] instructions;

    public LoadOptimizer(String instructions) {
        this.instructions = instructions.split("\n");
    }

    public String optimize() {
        StringBuilder optimizedInstructions = new StringBuilder();

        String previousStore = "";
        boolean previousWasStore = false;
        int storeRegister = -1;

        for (int index = 0; index < instructions.length; index++) {
            String instruction = instructions[index];

            List<String> splitInstruction = new java.util.ArrayList<>(List.of(instruction.split("[\\s_]")));

            if (splitInstruction.get(0).strip().equals("")) {
                splitInstruction.remove(0);
            }

            if (previousWasStore) {
                try {
                    int loadRegister = Integer.parseInt(splitInstruction.get(1));
                    Matcher patternMatcher = loadPattern.matcher(splitInstruction.get(0));

                    if (!(patternMatcher.find()) || storeRegister != loadRegister || isVarNeededLater(index, loadRegister)) {
                        optimizedInstructions.append(previousStore).append("\n");
                        optimizedInstructions.append(instruction).append("\n");
                    }
                } catch (Exception e) {
                    optimizedInstructions.append(previousStore).append("\n");
                    optimizedInstructions.append(instruction).append("\n");
                }

                previousStore = "";
                previousWasStore = false;
                storeRegister = -1;

                continue;
            }

            Matcher patternMatcher = storePattern.matcher(splitInstruction.get(0));

            if (patternMatcher.find()) {
                previousWasStore = true;
                previousStore = instruction;
                storeRegister = Integer.parseInt(splitInstruction.get(1));
                continue;
            }

            optimizedInstructions.append(instruction).append("\n");
        }

        return optimizedInstructions.toString();
    }

    private boolean isVarNeededLater(int instructionIndex, int varRegister) {
        Pattern loadRegisterPattern = Pattern.compile("\t[ia]load[\\s_]" + varRegister);
        Pattern storeRegisterPattern = Pattern.compile("\t[ia]store[\\s_]" + varRegister);
        for (int index = instructionIndex + 1; index < this.instructions.length; index++) {

            Matcher storeMatch = storeRegisterPattern.matcher(instructions[index]);
            if (storeMatch.find()) {
                return false;
            }

            Matcher loadMatch = loadRegisterPattern.matcher(instructions[index]);
            if (loadMatch.find()) {
                return true;
            }
        }
        return false;
    }
}
