package pt.up.fe.comp.semantic.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.semantic.JmmMethodSignature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocalVariablesVisitor extends AJmmVisitor<Map<String, List<Symbol>>, Boolean> {

    public LocalVariablesVisitor() {
        addVisit("Start", this::visitIntermediate);
        addVisit("ClassDeclaration", this::visitIntermediate);
        addVisit("RegularMethod", this::visitMethodDeclaration);
        addVisit("MainMethod", this::visitMethodDeclaration);

        setDefaultVisit((node, localVariables) -> true);
    }

    private Boolean visitIntermediate(JmmNode node, Map<String, List<Symbol>> localVariables) {
        for (JmmNode child: node.getChildren()) {
            this.visit(child, localVariables);
        }

        if (node.getKind().equals("Start")) {
            for (Map.Entry<String, List<Symbol>> entry : localVariables.entrySet()) {
                System.out.println("Method " + entry.getKey());
                List<Symbol> variables = entry.getValue();

                for (Symbol variable: variables) {
                    System.out.println(variable);
                }

                System.out.println("--------------");
            }
        }

        return true;
    }

    private Boolean visitMethodDeclaration(JmmNode methodDeclaration, Map<String, List<Symbol>> localVariables) {
        String name = "";
        List<Symbol> methodVariables = new ArrayList<>();

        for (JmmNode child: methodDeclaration.getChildren()) {
            if (child.getKind().equals("Identifier")) {
                name = child.get("name");
            }
            else if (child.getKind().equals("MethodBody")) {

                for (JmmNode line: child.getChildren()) {
                    if (line.getKind().equals("VarDeclaration")) {
                        JmmNode typeNode = line.getJmmChild(0), nameNode = line.getJmmChild(1);
                        Type varType;

                        if (typeNode.get("name").equals("intArray")) {
                            varType = new Type("int", true);
                        } else {
                            varType = new Type(typeNode.get("name"), false);
                        }

                        methodVariables.add(new Symbol(varType, nameNode.get("name")));
                    }
                }

            }
        }

        localVariables.put(name, methodVariables);

        return true;
    }
}
