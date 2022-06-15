package pt.up.fe.comp.optimization;

import org.specs.comp.ollir.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class LivenessAnalysis {

    HashMap<Node, ArrayList<Operand>> def;
    HashMap<Node, ArrayList<Operand>> use;

    HashMap<Node, ArrayList<Operand>> succ;
    HashMap<Node, ArrayList<Operand>> out;
    HashMap<Node, ArrayList<Operand>> in;
    public ArrayList<HashMap<Node, ArrayList<Operand>>> analyze(Method method) {
        def = new HashMap<>();
        use = new HashMap<>();
        succ = new HashMap<>();
        out = new HashMap<>();
        in = new HashMap<>();

        ArrayList<Instruction> instructionsList = method.getInstructions();

        for (Instruction instruction : instructionsList) {

            def.put(instruction, getDefs(instruction, method.getVarTable()));
            use.put(instruction, getUses(instruction));
            ArrayList<Operand> op = new ArrayList<>();
            in.put(instruction, op);
            out.put(instruction, op);

        }

        ArrayList<Instruction> nodes = new ArrayList<>(method.getInstructions());
        Collections.reverse(nodes);

        boolean somethingChanged;

        do {
            somethingChanged = false;

            HashMap<Node, ArrayList<Operand>> inTemp = new HashMap<>(in);
            HashMap<Node, ArrayList<Operand>> outTemp = new HashMap<>(out);

            for (Instruction instruction : nodes) {
                ArrayList<Operand> opList = new ArrayList<>();
                if (instruction.getSucc1() != null && instruction.getSucc1().getNodeType() != NodeType.END) {
                        opList = in.get(instruction.getSucc1());
                        if (instruction.getSucc2() != null) {
                            opList.addAll(in.get(instruction.getSucc2()));
                        }
                }
                out.replace(instruction, opList);
                ArrayList<Operand> temp_out = out.get(instruction);
                ArrayList<Operand> temp_def = def.get(instruction);
                int i = 0;
                if(temp_out.size() != 0) {
                    while (i < method.getVarTable().size()) {
                        if (temp_out.get(i) != null && (temp_def.get(i) != null)) {
                            temp_out.remove(i);
                            i--;
                        } else
                            temp_out.set(i, null);
                        i++;
                    }
                    temp_out.addAll(in.get(instruction.getSucc2()));
                    in.replace(instruction, temp_out);
                }
            }

            somethingChanged = true;
            for (Instruction instruction : nodes) {
                if (!in.get(instruction).equals(inTemp.get(instruction)) || !out.get(instruction).equals(outTemp.get(instruction)))
                    somethingChanged = false;
            }

        }while(!somethingChanged);

        ArrayList<HashMap<Node, ArrayList<Operand>>> result = new ArrayList<>();
        result.add(in);
        result.add(out);

        return result;
    }

    private ArrayList<Operand> getDefs(Instruction instruction, HashMap<String, Descriptor> varTable) {

        Element e;
        ArrayList<Operand> opList = new ArrayList<>();
        if (instruction.getInstType() == InstructionType.ASSIGN) {
            e = ((AssignInstruction) instruction).getDest();

            if (e.getType().getTypeOfElement() == ElementType.THIS) {
                return opList;
            }

            if (e.isLiteral()) return opList;

            Descriptor d = varTable.get(((Operand) e).getName());

            if (d.getScope() == VarScope.PARAMETER || d.getScope() == VarScope.FIELD) {
                return opList;
            }
            opList.add((Operand) e);
            return opList;
        }
        return opList;
    }

    private ArrayList<Operand> getUses(Instruction instruction) {

        ArrayList<Operand> list = new ArrayList<>();

        switch (instruction.getInstType()) {
            case CALL -> {
                CallInstruction callInst = (CallInstruction) instruction;

                try {
                    list.add((Operand) callInst.getFirstArg());
                }catch(ClassCastException ignored){}

                if (callInst.getNumOperands() > 1) {
                    if(callInst.getInvocationType() != CallType.NEW){
                        try {
                            list.add((Operand) callInst.getSecondArg());
                        }catch(ClassCastException ignored){}
                    }
                    for (Element arg : callInst.getListOfOperands()) {
                        try {
                            list.add((Operand) arg);
                        }catch(ClassCastException ignored){}
                    }
                }
                return list;
            }
            case NOPER -> {
                SingleOpInstruction singInst = (SingleOpInstruction) instruction;
                try {
                    list.add((Operand) singInst.getSingleOperand());
                }catch(ClassCastException ignored){}
            }
            case ASSIGN -> {
                AssignInstruction assignInst = (AssignInstruction) instruction;
                return getUses(assignInst.getRhs());
            }
            case BRANCH -> {
                CondBranchInstruction branchInst = (CondBranchInstruction) instruction;
                for (Element arg : branchInst.getOperands()) {
                    try {
                        list.add((Operand) arg);
                    }catch(ClassCastException ignored){}
                }
                return list;
            }
            case RETURN -> {
                ReturnInstruction retInst = (ReturnInstruction) instruction;
                if (retInst.hasReturnValue()) {
                    try {
                        list.add((Operand) retInst.getOperand());
                    }catch(ClassCastException ignored){}
                    return list;
                }
                return list;
            }

            case GETFIELD -> {
                GetFieldInstruction putInst = (GetFieldInstruction) instruction;
                try {
                    list.add((Operand) putInst.getFirstOperand());
                    list.add((Operand) putInst.getSecondOperand());
                }catch(ClassCastException ignored){}
                return list;
            }

            case PUTFIELD -> {
                PutFieldInstruction putInst = (PutFieldInstruction) instruction;
                try {
                    list.add((Operand) putInst.getThirdOperand());
                }catch(ClassCastException ignored){}
                return list;
            }

            case UNARYOPER -> {
                UnaryOpInstruction unaryInst = (UnaryOpInstruction) instruction;
                try {
                    list.add((Operand) unaryInst.getOperand());
                }catch(ClassCastException ignored){}
                return list;
            }
            case BINARYOPER -> {
                BinaryOpInstruction biInst = (BinaryOpInstruction) instruction;
                if (biInst.getOperands() == null) return list;
                for (Element arg : biInst.getOperands()) {
                    try {
                        list.add((Operand) arg);
                    }catch(ClassCastException ignored){}
                }
                return list;
            }
            default -> {
            }
        }
        return list;
    }

    private int indexInstructionByLabel(Method method, String label) {
        for (int i = 0; i < method.getInstructions().size(); i++){
            var labelsOfInstructions = method.getLabels(method.getInstr(i));
            if (labelsOfInstructions.size() > 0 && labelsOfInstructions.contains(label)) {
                return i+1;
            }
        }
        return -1;
    }

}