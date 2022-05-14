package pt.up.fe.comp.backend;

import freemarker.core.ast.Case;
import org.specs.comp.ollir.*;

public class InstructionTranslator {
    public String translateInstruction(Instruction instruction, Method ancestorMethod) {
        InstructionType instructionType = instruction.getInstType();

        switch (instructionType) {
            case CALL:
                return translateInstruction((CallInstruction) instruction, ancestorMethod);
            default:
                return "";
        }
    }

    public String translateInstruction(AssignInstruction instruction) {
        return "";
    }

    public String translateInstruction(CallInstruction instruction, Method ancestorMethod) {
        StringBuilder jasminInstruction = new StringBuilder();
        Element caller = instruction.getFirstArg();
        LiteralElement methodName = (LiteralElement) instruction.getSecondArg();

        CallType callType = instruction.getInvocationType();

        switch (callType) {
            case invokestatic:
            case invokevirtual:
            case invokespecial:
                if (callType == CallType.invokestatic) {
                    jasminInstruction.append("invokestatic ");
                } else if (callType == CallType.invokevirtual){
                    jasminInstruction.append("invokevirtual ");
                } if (callType == CallType.invokespecial) {
                    jasminInstruction.append("invokespecial ");
            }

                ClassType classType = (ClassType) instruction.getFirstArg().getType();

                jasminInstruction.append(JasminUtils.getFullClassName(ancestorMethod.getOllirClass(), classType.getName())).append(".").append(JasminUtils.trimLiteral(methodName.getLiteral()));
                jasminInstruction.append("(");

                for (Element element: instruction.getListOfOperands()) {
                    jasminInstruction.append(JasminUtils.translateType(ancestorMethod.getOllirClass(), element.getType()));
                }

                jasminInstruction.append(")").append(JasminUtils.translateType(ancestorMethod.getOllirClass(), instruction.getReturnType()));
            case NEW:
            case arraylength:
        }
        return jasminInstruction.toString();
    }

    public String translateInstruction(BinaryOpInstruction instruction) {
        return "";
    }

    public String translateInstruction(ReturnInstruction instruction) {
        return "return";
    }

    private String getCorrespondingLoad(Element element, Method ancestorMethod) {
        if (element.isLiteral()) {
            LiteralElement literalElement = (LiteralElement) element;

            switch (literalElement.getType().getTypeOfElement()) {
                case INT32:
                    return "ldc " + JasminUtils.trimLiteral(literalElement.getLiteral());
                case BOOLEAN:
                    String literal = JasminUtils.trimLiteral(literalElement.getLiteral());
                    return literal.equals("true") ? "ldc 1" : "ldc 0";
                default:
                    return "";
            }
        } else {
            Operand operand = (Operand) element;

            Descriptor operandDescriptor = ancestorMethod.getVarTable().get(operand.getName());

            switch (operand.getType().getTypeOfElement()) {
                case INT32:
                case BOOLEAN:
                    return "iload " + operandDescriptor.getVirtualReg();
                case CLASS:
                case OBJECTREF:
                case THIS:
                case STRING:
                    return "aload " + operandDescriptor.getVirtualReg();
                default:
                    return "";
            }
        }
    }
}
