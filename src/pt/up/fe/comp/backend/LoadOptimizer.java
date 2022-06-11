package pt.up.fe.comp.backend;

public class LoadOptimizer {
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

            String[] splitInstruction = instruction.split(" ");

            if (previousWasStore) {
                try {
                    int loadRegister = Integer.parseInt(splitInstruction[1]);

                    if (!(splitInstruction[0].equals("\taload") || splitInstruction[0].equals("\tiload"))
                            || storeRegister != loadRegister
                            || isVarNeededLater(index, loadRegister)) {
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

            if (splitInstruction[0].equals("\tastore") || splitInstruction[0].equals("\tistore")) {
                previousWasStore = true;
                previousStore = instruction;
                storeRegister = Integer.parseInt(splitInstruction[1]);
                continue;
            }

            optimizedInstructions.append(instruction).append("\n");
        }

        return optimizedInstructions.toString();
    }

    private boolean isVarNeededLater(int instructionIndex, int varRegister) {
        for (int index = instructionIndex + 1; index < this.instructions.length; index++) {
            if (instructions[index].equals("\tistore " + varRegister) || instructions[index].equals("\tastore " + varRegister)) {
                return false;
            }

            if (instructions[index].equals("\tiload " + varRegister) || instructions[index].equals("\taload " + varRegister)) {
                return true;
            }
        }
        return false;
    }
}
