package pt.up.fe.comp.semantic.visitors.symbolTableBuilder;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.types.JmmClassSignature;
import pt.up.fe.comp.semantic.visitors.ReportCollectorJmmNodeVisitor;

import java.util.Optional;

public class ClassDeclarationVisitor extends ReportCollectorJmmNodeVisitor<JmmClassSignature, Boolean> {
    private final String className;

    public ClassDeclarationVisitor(String inputFile) {
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("VarDeclaration", this::visitVarDeclaration);
        setDefaultVisit((node, classSignature) -> true);
        className = getClassNameFromFileName(inputFile);
    }

    private String getClassNameFromFileName(String inputFile) {
        if (inputFile == null){
            return null;
        }
        inputFile = inputFile.substring(inputFile.lastIndexOf('/') + 1);
        inputFile = inputFile.substring(0, inputFile.indexOf('.'));
        return inputFile;
    }

    private Boolean visitStart(JmmNode startNode, JmmClassSignature classSignature) {
        for (JmmNode child : startNode.getChildren()) {
            visit(child, classSignature);
        }
        return true;
    }

    private Boolean visitClassDeclaration(JmmNode classNode, JmmClassSignature classSignature) {
        if (className != null && !className.equals(classNode.get("name"))){
            addSemanticErrorReport(classNode, "The name of the class must match the filename");
        }

        classSignature.setClassName(classNode.get("name"));
        Optional<String> superName = classNode.getOptional("extends");
        superName.ifPresent(classSignature::setSuperName);

        for (JmmNode child : classNode.getChildren()) {
            visit(child, classSignature);
        }
        return true;
    }

    private Boolean visitVarDeclaration(JmmNode varDeclarationNode, JmmClassSignature classSignature) {
        JmmNode typeNode = varDeclarationNode.getJmmChild(0);
        JmmNode nameNode = varDeclarationNode.getJmmChild(1);
        Type fieldType = new Type(typeNode.get("name"), Boolean.parseBoolean(typeNode.get("isArray")));
        classSignature.getFields().add(new Symbol(fieldType, nameNode.get("name")));
        return true;
    }
}
