package pt.up.fe.comp.graph;

import java.util.*;

public class GraphNode {
    String varName;
    int originalReg;
    ArrayList<GraphEdge> edges;
    boolean active;

    public GraphNode(String name, int reg) {
        varName = name;
        originalReg = reg;
        edges = new ArrayList<>();
        active = true;
    }

    public void addEdge(GraphEdge edge) {
        if (!edges.contains(edge))
            edges.add(edge);
    }

    public void removeEdge(GraphEdge edge) {
        edges.remove(edge);
    }

    public int getNumEdges() {
        return getLinkedNodes().size();
    }

    public int getOriginalReg() {
        return originalReg;
    }

    public String getName() { return varName; }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    public ArrayList<GraphEdge> removeEdges(){
        ArrayList<GraphEdge> copy = edges;
        for (GraphEdge edge: edges) {
            removeEdge(edge);
        }
        return copy;
    }

    public ArrayList<String> getLinkedNodes() {
        ArrayList<String> vars = new ArrayList<>();
        for (GraphEdge edge: edges) {
            if (edge.getSecond().isActive())
                vars.add(edge.getSecond().getName());
        }
        return vars;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphNode graphNode = (GraphNode) o;
        return originalReg == graphNode.originalReg && Objects.equals(varName, graphNode.varName) && Objects.equals(edges, graphNode.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(varName, originalReg, edges);
    }

    @Override
    public String toString() {
        if (!active)
            return "";
        return "graph.GraphNode{" +
                "varname='" + varName + '\'' +
                ", originalReg=" + originalReg +
                ", edges=" + edges +
                '}';
    }
}