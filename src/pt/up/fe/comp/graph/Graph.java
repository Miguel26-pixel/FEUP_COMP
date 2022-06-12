package pt.up.fe.comp.graph;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.graphs.GraphNode;

import java.util.ArrayList;
import java.util.HashMap;

public class Graph {
        HashMap<Integer, Node> nodes;
        int minReg;
        boolean staticMethod;
        HashMap<String, Descriptor> varTable;

        public Graph(ArrayList<HashMap<Node, ArrayList<Operand>>> liveRanges, Method method) {
                nodes = new HashMap<>();

                varTable = method.getVarTable();
                staticMethod = method.isStaticMethod();
                minReg = staticMethod ? 0 : 1;

                for(String name: varTable.keySet()) {
                        Descriptor d = varTable.get(name);
                        if (d.getScope() == VarScope.PARAMETER || d.getScope() == VarScope.FIELD)
                                minReg++;
                        else
                                nodes.put(d.getVirtualReg(), new GraphNode(name, d.getVirtualReg()));
                }
        }
}
