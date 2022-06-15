package pt.up.fe.comp.optimization;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegAlloc {

    private final ClassUnit classUnit;
    private final LivenessAnalysis liveAnalyzer;
    private final List<Report> reportsList;

    public RegAlloc(ClassUnit cUnit, List<Report> reports) {
        classUnit = cUnit;
        liveAnalyzer = new LivenessAnalysis();
        reportsList = reports;
    }

    public void allocateRegs(int numberOfRegs) {
        classUnit.buildCFGs();
        classUnit.buildVarTables();

        for (Method method : classUnit.getMethods()) {
            ArrayList<HashMap<Node, ArrayList<Operand>>> liveRanges = liveAnalyzer.analyze(method);
            InterferenceGraph intGraph = new InterferenceGraph(liveRanges, method, reportsList);

            HashMap<String, Descriptor> v2Table = intGraph.graphColoring(numberOfRegs);

            if (v2Table != null) {
                for (var entry : method.getVarTable().entrySet()) {
                    for (var entry2 : v2Table.entrySet()) {
                        if (entry.getKey().equals(entry2.getKey())) {
                            method.getVarTable().replace(entry.getKey(), entry.getValue(), entry2.getValue());
                        }
                        if (!v2Table.containsKey(entry.getKey())) {
                            method.getVarTable().remove(entry.getKey());
                        }
                    }
                }

            }
        }
    }
}
