package pt.up.fe.comp.semantic.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.types.JmmMethodSignature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MethodDeclarationVisitor extends AJmmVisitor<Map<String, JmmMethodSignature>, Boolean> {

    public MethodDeclarationVisitor() {
        addVisit("Start", this::visitIntermediate);
        addVisit("ClassDeclaration", this::visitIntermediate);
        addVisit("RegularMethod", this::visitRegularMethodDeclaration);
        addVisit("MainMethod", this::visitMainMethodDeclaration);

        setDefaultVisit((node, methods) -> true);
    }

    private Boolean visitIntermediate(JmmNode node, Map<String, JmmMethodSignature> methods) {
        for (JmmNode child: node.getChildren()) {
            this.visit(child, methods);
        }
        return true;
    }

    private Boolean visitRegularMethodDeclaration(JmmNode methodDeclaration, Map<String, JmmMethodSignature> methods) {
        String name = "";
        Type returnType = new Type("", false);
        List<Symbol> parameters = new ArrayList<>();

        for (JmmNode child: methodDeclaration.getChildren()) {
            if (child.getKind().equals("Parameters")) {
                for (JmmNode parameterNode: child.getChildren()) {
                    List<JmmNode> typeNamePair = parameterNode.getChildren();
                    String parameterTypeName = typeNamePair.get(0).get("name");
                    boolean parameterTypeIsArray = Boolean.parseBoolean(typeNamePair.get(0).get("isArray"));
                    String parameterName = typeNamePair.get(1).get("name");
                    parameters.add(new Symbol(new Type(parameterTypeName, parameterTypeIsArray), parameterName));
                }
            } else if (child.getKind().equals("Identifier")) {
                name = child.get("name");
            } else if (child.getKind().equals("Type")) {
                returnType = new Type(child.get("name"), Boolean.parseBoolean(child.get("isArray")));
            }
        }

        methods.put(name, new JmmMethodSignature(returnType, parameters));
        return true;
    }

    private Boolean visitMainMethodDeclaration(JmmNode methodDeclaration, Map<String, JmmMethodSignature> methods) {
        for (JmmNode child: methodDeclaration.getChildren()) {
            if (child.getKind().equals("Parameter")) {
                Symbol parameter = new Symbol(new Type("String", true), child.getJmmChild(0).get("name"));
                methods.put("main", new JmmMethodSignature(new Type("void",false), List.of(parameter)));
            }
        }
        return true;
    }
}
