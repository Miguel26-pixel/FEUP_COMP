package pt.up.fe.comp.optimization;

import org.eclipse.jgit.ignore.IgnoreNode;
import org.specs.comp.ollir.*;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Node;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;

public class LivenessAnalysis {
    public ArrayList<HashMap<Node, ArrayList<Operand>>> LivenessAnalysis(Method method) {
        HashMap<Node, ArrayList<Operand>> def = new HashMap<>();
        HashMap<Node, ArrayList<Operand>> use = new HashMap<>();
        HashMap<Node, ArrayList<Operand>> out = new HashMap<>();
        HashMap<Node, ArrayList<Operand>> in = new HashMap<>();

        ArrayList<Instruction> instructionsList = method.getInstructions();

        int n_vars = method.getVarTable().size();
        for (Instruction instruction : instructionsList) {

            def.put(instruction, getDefVars(instruction, method.getVarTable()));
            use.put(instruction, isUseVars(instruction, method.getVarTable()));
            in.put(instruction, null);
            out.put(instruction,null);

        }

        boolean done = false;
        int i = 0;
        ArrayList<Instruction> nodes = new ArrayList<>(method.getInstructions());
        Collections.reverse(nodes);

        while(!done){
            System.out.println("iteration " + i);
            i++;
            HashMap<Node, ArrayList<Operand>> in_temp = new HashMap<>(in);
            HashMap<Node, ArrayList<Operand>> out_temp = new HashMap<>(out);

            for (Instruction instruction : nodes) {
                ArrayList<Operand> opList = new ArrayList<>();
                if (instruction.getSucc1() != null) {
                    if (instruction.getSucc1().getNodeType() != NodeType.END) {
                        opList = in.get(instruction.getSucc1());
                        if (instruction.getSucc2() != null) {
                            opList = orArray(opList,in.get(instruction.getSucc2()));
                        }
                    }
                }
                out.replace(instruction, opList);
                ArrayList<Operand> opList2 = out.get(instruction);
                ArrayList<Operand> temp_def = def.get(instruction);
                for (int index = 0; index < n_vars; index++) {
                    if (opList2.get(index) != null && (temp_def.get(index) != null))
                        opList2.remove(index);
                    else
                        opList2.set(index, null);
                }
                opList2 = orArray(opList2,in.get(instruction.getSucc2()));
                in.replace(instruction, opList2);
            }

            //printTable(use, def, in, out, nodes, n_vars + 1);

            done = true;
            for (Instruction instruction : nodes) {
                if (!in.get(instruction).equals(in_temp.get(instruction)))
                    done = false;
                if (!out.get(instruction).equals(out_temp.get(instruction)))
                    done = false;
            }
        }
        ArrayList<HashMap<Node, ArrayList<Operand>>> result = new ArrayList<>();
        result.add(in);
        result.add(out);

        return result;
    }

    private ArrayList<Operand> getDefVars(Instruction instruction, HashMap<String, Descriptor> varTable) {

        Element e;
        ArrayList<Operand> opList = new ArrayList<>();
        if (instruction.getInstType() == InstructionType.ASSIGN) {
            e = ((AssignInstruction) instruction).getDest();

            if (e.isLiteral()) return null;

            Descriptor d = varTable.get(((Operand) e).getName());

            if(d.getScope() == VarScope.PARAMETER || d.getScope() == VarScope.FIELD) {
                return null;
            }
            opList.add((Operand) e);
            return opList;
        }
        return null;
    }

    private ArrayList<Operand> isUseVars(Instruction instruction, HashMap<String, Descriptor> varTable) {

        switch (instruction.getInstType()) {
            case CALL:
                return getUsedVarsCall((CallInstruction) instruction, varTable);
            case NOPER:
                return getUsedVarsSingleOp((SingleOpInstruction) instruction, varTable);
            case ASSIGN:
                return getUsedVarsAssign((AssignInstruction) instruction, varTable);
            case BRANCH:
                return getUsedVarsBranch((CondBranchInstruction) instruction, varTable);
            case RETURN:
                return getUsedVarsReturn((ReturnInstruction) instruction, varTable);
            case GETFIELD:
                return getUsedVarsGetField((GetFieldInstruction) instruction, varTable);
            case PUTFIELD:
                return getUsedVarsPutField((PutFieldInstruction) instruction, varTable);
            case UNARYOPER:
                return getUsedVarsUnaryOp((UnaryOpInstruction) instruction, varTable);
            case BINARYOPER:
                return getUsedVarsBinaryOp((BinaryOpInstruction) instruction, varTable);
            default:
                break;
        }
        return null;
    }

    private ArrayList<Operand> getUsedVarsBinaryOp(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        ArrayList<Operand> opList = new ArrayList<>();
        for (Element arg : instruction.getOperands()){
            opList.add((Operand) arg);
        }
        return opList;
    }

    private ArrayList<Operand> getUsedVarsUnaryOp(UnaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        ArrayList<Operand> opList = new ArrayList<>();
        opList.add((Operand) instruction.getOperand());
        return opList;
    }

    private ArrayList<Operand> getUsedVarsPutField(PutFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        ArrayList<Operand> opList = new ArrayList<>();
        opList.add((Operand) instruction.getThirdOperand());
        return opList;
    }

    private ArrayList<Operand> getUsedVarsGetField(FieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        ArrayList<Operand> opList = new ArrayList<>();
        opList.add((Operand) instruction.getFirstOperand());
        opList.add((Operand) instruction.getSecondOperand());
        return opList;
    }

    private ArrayList<Operand> getUsedVarsReturn(ReturnInstruction instruction, HashMap<String, Descriptor> varTable) {
        ArrayList<Operand> opList = new ArrayList<>();
        if (instruction.hasReturnValue())
            opList.add((Operand) instruction.getOperand());
        return null;
    }

    private ArrayList<Operand> getUsedVarsBranch(CondBranchInstruction instruction, HashMap<String, Descriptor> varTable) {
        ArrayList<Operand> opList = new ArrayList<>();
        for (Element arg : instruction.getOperands()){
            opList.add((Operand) arg);
        }
        return opList;
    }

    private ArrayList<Operand> getUsedVarsAssign(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {
        return isUseVars(instruction.getRhs(), varTable);
    }

    private ArrayList<Operand> getUsedVarsSingleOp(SingleOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        ArrayList<Operand> opList = new ArrayList<>();
        opList.add((Operand) instruction.getSingleOperand());
        return opList;
    }

    private ArrayList<Operand> getUsedVarsCall(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        ArrayList<Operand> opList = new ArrayList<>();
        for (Element arg : instruction.getListOfOperands()){
            opList.add((Operand) arg);
        }
        return opList;
    }

    private ArrayList<Operand> orArray(ArrayList<Operand> opList,ArrayList<Operand> list2) {
        for (Operand op : list2){
            opList.add(op);
        }
        return opList;
    }
}
