package pt.up.fe.comp.graph;

import java.util.Objects;

public class GraphEdge {
    GraphNode first;
    GraphNode second;

    public GraphEdge(GraphNode n1, GraphNode n2) {
        first = n1;
        second = n2;
    }

    public GraphNode getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphEdge graphEdge = (GraphEdge) o;
        return Objects.equals(first, graphEdge.first) && Objects.equals(second, graphEdge.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "graph.GraphEdge{" +
                "" + first.getName() +
                "," + second.getName() +
                '}';
    }
}