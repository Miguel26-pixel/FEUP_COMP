package pt.up.fe.comp.semantic.visitors.symbolTableBuilder;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.visitors.ReportCollectorJmmNodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocalVariablesVisitor extends ReportCollectorJmmNodeVisitor<Map<String, List<Symbol>>, Boolean> {

    public LocalVariablesVisitor() {
        addVisit("Start", this::visitIntermediate);
        addVisit("ClassDeclaration", this::visitIntermediate);
        addVisit("RegularMethod", this::visitMethodDeclaration);
        addVisit("MainMethod", this::visitMethodDeclaration);

        setDefaultVisit((node, localVariables) -> true);
    }

    private Boolean visitIntermediate(JmmNode node, Map<String, List<Symbol>> localVariables) {
        for (JmmNode child : node.getChildren()) {
            this.visit(child, localVariables);
        }
        return true;
    }

    private Boolean visitMethodDeclaration(JmmNode methodDeclaration, Map<String, List<Symbol>> localVariables) {
        String name = "main";
        List<Symbol> methodVariables = new ArrayList<>();

        for (JmmNode child : methodDeclaration.getChildren()) {
            if (child.getKind().equals("Identifier")) {
                name = child.get("name");
            } else if (child.getKind().equals("MethodBody")) {
                for (JmmNode line : child.getChildren()) {
                    if (!line.getKind().equals("VarDeclaration")) {
                        continue;
                    }
                    JmmNode typeNode = line.getJmmChild(0), nameNode = line.getJmmChild(1);
                    Type variableType = new Type(typeNode.get("name"), Boolean.parseBoolean(typeNode.get("isArray")));
                    methodVariables.add(new Symbol(variableType, nameNode.get("name")));
                }
            }
        }

        localVariables.put(name, methodVariables);
        return true;
    }
}
