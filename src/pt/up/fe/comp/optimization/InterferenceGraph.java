package pt.up.fe.comp.optimization;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

public class InterferenceGraph {

    private final HashMap<Integer, Set<Integer>> interferenceGraph;
    private final HashMap<Integer, ArrayList<Integer>> colors;

    private Integer minReg;

    private final List<Report> reportsList;

    private final HashMap<String, Descriptor> varTable;

    private final ArrayList<HashMap<Node, ArrayList<Operand>>> liveArray;

    private Integer numberOfRegisters;

    private final Stack<Integer> stack;

    private final Method method;

    public InterferenceGraph(ArrayList<HashMap<Node, ArrayList<Operand>>> liveRanges, Method m, List<Report> reports) {
        interferenceGraph = new HashMap<>();
        colors = new HashMap<>();

        reportsList = reports;
        liveArray = liveRanges;
        method = m;
        minReg = method.isStaticMethod() ? 0 : 1;
        varTable = method.getVarTable();
        stack = new Stack<>();

        buildIntGraph();
    }

    public void buildIntGraph() {

        for(String s: varTable.keySet()) {
            Descriptor d = varTable.get(s);
            if(d.getScope() == VarScope.PARAMETER || d.getScope() == VarScope.FIELD) minReg++;
            else interferenceGraph.put(d.getVirtualReg(),new HashSet<>());
        }

        for (HashMap<Node, ArrayList<Operand>> liveHash : liveArray) {

            for (Node entry: liveHash.keySet()){

                        for (Operand op1 : liveHash.get(entry)) {
                            for (Operand op2 : liveHash.get(entry)) {

                                if (op1.getName().equals(op2.getName())) continue;

                                Descriptor d = varTable.get(op1.getName());
                                if (interferenceGraph.get(d.getVirtualReg()) != null) {
                                    interferenceGraph.get(d.getVirtualReg()).add(varTable.get(op2.getName()).getVirtualReg());

                                }
                            }

                        }
                    }

        }
    }

    public HashMap<String, Descriptor> graphColoring(int nRegisters) {

        numberOfRegisters = nRegisters;
        if(numberOfRegisters==0){
            numberOfRegisters = minReg; //if the numbers of registers given are 0 then we try to use the minimum
        }
        else if (numberOfRegisters < minReg) {
            reportsList.add(new Report(ReportType.ERROR, Stage.OPTIMIZATION,-1,"Insufficient registers to store this method's variables, you need at least " + minReg + " registers."));
            System.exit(-1);
        }

        for (int i = minReg; i < numberOfRegisters; i++){
            colors.put(i,new ArrayList<>());
        }

        HashMap<Integer, Set<Integer>> copy = new HashMap<>(interferenceGraph);
        upToStack(copy);

        while(!stack.isEmpty()){
            Integer var = stack.pop();

            boolean canBeColored = false;
            for(var color : colors.keySet()) {
                boolean toBeColored = true;
                for (var connectedVar : interferenceGraph.get(var)) {
                    if (colors.get(color).contains(connectedVar)) {
                        toBeColored = false;
                        break;
                    }
                }
                if (toBeColored) {
                    colors.get(color).add(var);
                    Descriptor oldD = varTable.get(getNameByVirtualReg(var));
                    canBeColored = true;
                    break;
                }
            }
            if (!canBeColored) {
                while(!stack.isEmpty()){ //need to pop or will be a stackMemory error
                    stack.pop();
                }
                return graphColoring(numberOfRegisters+1); //number of registers not enough, can't color anything, trying adding one
            }
        }
        return finalNewVarTableUpdate();
    }

    private void upToStack(HashMap<Integer, Set<Integer>> interGraph){

        while (!interGraph.isEmpty()) {
            for(var entry: interGraph.entrySet())
                if (entry.getValue().size() < numberOfRegisters) {
                    stack.push(entry.getKey());
                    interGraph.remove(entry.getKey());
                    break;
                }
            }

    }

    private String getNameByVirtualReg(Integer virtualReg){
        for (var temp : varTable.entrySet()){
            if(virtualReg == temp.getValue().getVirtualReg()){
                return temp.getKey();
            }
        }
        return null;
    }

    private Descriptor getDescriptorByVirtualReg(Integer virtualReg){
        for (var temp : varTable.entrySet()){
            if(virtualReg == temp.getValue().getVirtualReg()){
                return temp.getValue();
            }
        }
        return null;
    }

    private HashMap<String, Descriptor> finalNewVarTableUpdate() {

        HashMap<String, Descriptor> v2Table = varTable;
        for(var color : colors.entrySet()){
            for(Integer intSet : color.getValue()) {
                for (var vTentry : v2Table.entrySet()) {
                    if (vTentry.getValue() == getDescriptorByVirtualReg(intSet)){
                        Descriptor oldD = vTentry.getValue();
                        vTentry.setValue(new Descriptor(oldD.getScope(), color.getKey(), oldD.getVarType()));
                    }
                }
            }
        }

        return v2Table;

    }
}
