package pt.up.fe.comp.graph;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

public class Graph {
        HashMap<Integer, GraphNode> nodes;
        int minReg;
        boolean staticMethod;
        HashMap<String, Descriptor> varTable;

        List<Report> reportsList;

        public Graph(ArrayList<HashMap<Node, ArrayList<Operand>>> liveRanges, Method method, List<Report> reports) {
                nodes = new HashMap<>();

                varTable = method.getVarTable();
                reportsList = reports;
                staticMethod = method.isStaticMethod();
                minReg = staticMethod ? 0 : 1;

                for(String name: varTable.keySet()) {
                        Descriptor d = varTable.get(name);
                        if (d.getScope() == VarScope.PARAMETER || d.getScope() == VarScope.FIELD)
                                minReg++;
                        else
                                nodes.put(d.getVirtualReg(), new GraphNode(name, d.getVirtualReg()));
                }

                for(HashMap<Node, ArrayList<Operand>> live: liveRanges) {
                        for (Node node : live.keySet()) {
                                ArrayList<Operand> opList = live.get(node);

                                for (int i = 0; i < opList.size() - 1; i++) {
                                        GraphNode nodeEdited = nodes.get(varTable.get(opList.get(i).getName()).getVirtualReg());
                                        for (int j = i + 1; j < opList.size(); j++) {
                                                GraphNode secondNode = nodes.get(varTable.get(opList.get(j).getName()).getVirtualReg());
                                                nodeEdited.addEdge(new GraphEdge(nodeEdited, secondNode));
                                                secondNode.addEdge(new GraphEdge(secondNode, nodeEdited));
                                        }
                                }
                        }

                        }
        }

        public HashMap<String, Descriptor> graphColoring(int k) {
                if(k==0){
                        k=minReg;
                }
                else if (k < minReg) {
                        reportsList.add(new Report(ReportType.ERROR, Stage.OPTIMIZATION,-1,"Insufficient registers to store this method's variables, you need at least " + minReg + " registers."));
                        return null;

                }

                Stack<GraphNode> stack = new Stack<>();

                while (!nodes.isEmpty()) {
                        Iterator<Map.Entry<Integer, GraphNode>> it = nodes.entrySet().iterator();
                        while (it.hasNext()) {
                                GraphNode node = it.next().getValue();
                                if (node.getNumEdges() < k) {
                                        stack.push(node);
                                        node.setActive(false);
                                        it.remove();
                                }
                        }
                }

                HashMap<Integer, ArrayList<String>> colors = new HashMap<>();
                for (int i = minReg; i < k; i++)
                        colors.put(i, new ArrayList<>());

                HashMap<String, Descriptor> newVarTable = new HashMap<>();
                while (!stack.isEmpty()) {
                        GraphNode node = stack.pop();
                        node.setActive(true);
                        nodes.put(node.getOriginalReg(), node);

                        boolean colored = false;
                        for (Integer reg : colors.keySet()) {
                                boolean canColor = true;
                                for (String var : node.getLinkedNodes()) {
                                        if (colors.get(reg).contains(var))
                                                canColor = false;
                                }

                                if (canColor) {
                                        colors.get(reg).add(node.getName());
                                        Descriptor old = varTable.get(node.getName());
                                        newVarTable.put(node.getName(), new Descriptor(old.getScope(), reg, old.getVarType()));
                                        colored = true;
                                        break;
                                }
                        }
                        if (!colored) {
                                System.out.println(k + " -- Insufficient registers to store this method's variables");
                                while (!stack.isEmpty()) {
                                        GraphNode n = stack.pop();
                                        n.setActive(true);
                                        nodes.put(n.getOriginalReg(), n);
                                }
                                return graphColoring(k+1);
                        }
                }

                int reg = staticMethod ? 0 : 1;
                for (String name: varTable.keySet()) {
                        Descriptor d = varTable.get(name);
                        if (d.getScope() == VarScope.PARAMETER || d.getScope() == VarScope.FIELD) {
                                newVarTable.put(name, new Descriptor(d.getScope(), reg, d.getVarType()));
                                reg++;
                        }
                }

                ArrayList<Integer> used_reg = new ArrayList<>();
                for (Descriptor d: newVarTable.values()) {
                        if(!used_reg.contains(d.getVirtualReg()))
                                used_reg.add(d.getVirtualReg());
                }
                if (!used_reg.contains(0))
                        used_reg.add(0);

                System.out.println("Allocated " + used_reg.size() + " registers");
                return newVarTable;

        }
}
