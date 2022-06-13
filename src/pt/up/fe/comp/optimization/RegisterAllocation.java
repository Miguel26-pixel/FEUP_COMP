package pt.up.fe.comp.optimization;

import pt.up.fe.comp.graph.Graph;
import org.specs.comp.ollir.*;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Node;
import pt.up.fe.comp.jmm.report.Report;

import java.util.*;

public class RegisterAllocation {
    private final ClassUnit classUnit;
    private final LivenessAnalysis livenessAnalyzer;

    private List<Report> reports;

    public RegisterAllocation(ClassUnit classUnit, List<Report> reports) {
        this.classUnit = classUnit;
        this.livenessAnalyzer = new LivenessAnalysis();
        this.reports = reports;
    }

    public void allocateRegisters(int n) {
        try {
            classUnit.checkMethodLabels(); // check the use of labels in the OLLIR loaded
            classUnit.buildCFGs(); // build the CFG of each method
            classUnit.buildVarTables(); // build the table of variables for each method
        } catch (OllirErrorException e) {
            e.printStackTrace();
            return;
        }
        for (Method method : classUnit.getMethods()) {
            System.out.println("semantic.Method: " + method.getMethodName());

            ArrayList<HashMap<Node, ArrayList<Operand>>> liveRanges = livenessAnalyzer.analyze(method);
            Graph varGraph = new Graph(liveRanges, method, reports);

            HashMap<String, Descriptor> v2Table = varGraph.graphColoring(n);

            if(v2Table != null) {

                method.getVarTable().clear();

                for (String s : v2Table.keySet()) {
                    method.getVarTable().put(s, v2Table.get(s));
                }
            }
        }
    }
}
