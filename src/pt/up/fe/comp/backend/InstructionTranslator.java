package pt.up.fe.comp.backend;

import org.specs.comp.ollir.*;

public class InstructionTranslator {
    public String translateInstruction(Instruction instruction, ClassUnit ollirClass) {
        InstructionType instructionType = instruction.getInstType();

        switch (instructionType) {
            case CALL:
                return translateInstruction((CallInstruction) instruction, ollirClass);
            default:
                return "";
        }
    }

    public String translateInstruction(AssignInstruction instruction) {
        return "";
    }

    public String translateInstruction(CallInstruction instruction, ClassUnit ollirClass) {
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

                jasminInstruction.append(JasminUtils.getFullClassName(ollirClass, classType.getName())).append(".").append(JasminUtils.trimLiteral(methodName.getLiteral()));
                jasminInstruction.append("(");

                for (Element element: instruction.getListOfOperands()) {
                    jasminInstruction.append(JasminUtils.translateType(ollirClass, element.getType()));
                }

                jasminInstruction.append(")").append(JasminUtils.translateType(ollirClass, instruction.getReturnType()));
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
}
