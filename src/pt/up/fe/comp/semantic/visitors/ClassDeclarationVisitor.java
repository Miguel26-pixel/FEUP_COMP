package pt.up.fe.comp.semantic.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.types.JmmClassSignature;

import java.util.Optional;

public class ClassDeclarationVisitor extends AJmmVisitor<JmmClassSignature, Boolean> {

    public ClassDeclarationVisitor(){
        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("VarDeclaration", this::visitVarDeclaration);
        setDefaultVisit((node, classSignature) -> true);
    }

    private Boolean visitStart(JmmNode startNode, JmmClassSignature classSignature) {
        for (JmmNode child: startNode.getChildren()) {
            visit(child, classSignature);
        }

        System.out.println(classSignature);
        return true;
    }

    private Boolean visitClassDeclaration(JmmNode classNode, JmmClassSignature classSignature){
        classSignature.setClassName(classNode.get("name"));
        Optional<String> superName = classNode.getOptional("extends");
        superName.ifPresent(classSignature::setSuperName);

        for (JmmNode child: classNode.getChildren()){
            visit(child, classSignature);
        }
        return true;
    }

    private Boolean visitVarDeclaration(JmmNode varDeclarationNode, JmmClassSignature classSignature){
        JmmNode typeNode = varDeclarationNode.getJmmChild(0);
        JmmNode nameNode = varDeclarationNode.getJmmChild(1);
        Type fieldType;

        if (typeNode.get("name").equals("intArray")) {
            fieldType = new Type("int", true);
        } else {
            fieldType = new Type(typeNode.get("name"), false);
        }

        classSignature.getFields().add(new Symbol(fieldType, nameNode.get("name")));
        return true;
    }
}
