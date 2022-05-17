package pt.up.fe.comp.backend;

import org.specs.comp.ollir.*;

public class InstructionTranslator {
    public String translateInstruction(Instruction instruction, Method ancestorMethod, int indentationLevel) {
        InstructionType instructionType = instruction.getInstType();

        switch (instructionType) {
            case CALL:
                return translateInstruction((CallInstruction) instruction, ancestorMethod, indentationLevel);
            case RETURN:
                return translateInstruction((ReturnInstruction) instruction, ancestorMethod, indentationLevel);
            case PUTFIELD:
                return translateInstruction((PutFieldInstruction) instruction, ancestorMethod, indentationLevel);
            case GETFIELD:
                return translateInstruction((GetFieldInstruction) instruction, ancestorMethod, indentationLevel);
            case ASSIGN:
                return translateInstruction((AssignInstruction) instruction, ancestorMethod, indentationLevel);
            case BINARYOPER:
                return translateInstruction((BinaryOpInstruction) instruction, ancestorMethod, indentationLevel);
            case UNARYOPER:
                return translateInstruction((UnaryOpInstruction) instruction, ancestorMethod, indentationLevel);
            case NOPER:
                return translateInstruction((SingleOpInstruction) instruction, ancestorMethod, indentationLevel);
            default:
                return "";
        }
    }

    public String translateInstruction(UnaryOpInstruction instruction, Method ancestorMethod, int indentationLevel) {
        Operation operation = instruction.getOperation();
        OperationType operationType = operation.getOpType();
        Element first = instruction.getOperand();

        if (operationType == OperationType.NOT || operationType == OperationType.NOTB) {
            StringBuilder jasminInstruction = new StringBuilder();

            if (first.getType().getTypeOfElement() != operation.getTypeInfo().getTypeOfElement()) {
                return "UNMATCHING TYPES";
            }

            if (operation.getTypeInfo().getTypeOfElement() != ElementType.BOOLEAN) {
                return "INCORRECT TYPE";
            }

            jasminInstruction.append(getIndentation(indentationLevel)).append(getCorrespondingLoad(first, ancestorMethod)).append("\n");
            jasminInstruction.append(getIndentation(indentationLevel)).append("ineg");

            return jasminInstruction.toString();
        }

        return "UNSUPPORTED UNARY OPERATION";
    }

    public String translateInstruction(SingleOpInstruction instruction, Method ancestorMethod, int indentationLevel) {
        return getIndentation(indentationLevel) + getCorrespondingLoad(instruction.getSingleOperand(), ancestorMethod);
    }

    public String translateInstruction(GetFieldInstruction instruction, Method ancestorMethod, int indentationLevel) {
        Element destinationObject = instruction.getFirstOperand();
        Element destinationField = instruction.getSecondOperand();

        if (destinationObject.isLiteral() || destinationField.isLiteral()) {
            return "THERE ARE NO FIELD LITERALS";
        }

        StringBuilder jasminInstruction = new StringBuilder();

        if (destinationObject.getType().getTypeOfElement() == ElementType.OBJECTREF || destinationObject.getType().getTypeOfElement() == ElementType.THIS) {
            jasminInstruction.append(getIndentation(indentationLevel)).append(getCorrespondingLoad(destinationObject, ancestorMethod)).append("\n");
            jasminInstruction.append(getIndentation(indentationLevel)).append("getfield ");
        } else if (destinationObject.getType().getTypeOfElement() == ElementType.CLASS) {
            jasminInstruction.append(getIndentation(indentationLevel)).append("getstatic ");
        }

        ClassType classType = (ClassType) destinationObject.getType();

        jasminInstruction.append(classType.getName()).append("/").append(((Operand) destinationField).getName());
        jasminInstruction.append(" ").append(JasminUtils.translateType(ancestorMethod.getOllirClass(), destinationField.getType()));

        return jasminInstruction.toString();
    }

    public String translateInstruction(PutFieldInstruction instruction, Method ancestorMethod, int indentationLevel) {
        Element destinationObject = instruction.getFirstOperand();
        Element destinationField = instruction.getSecondOperand();

        if (destinationObject.isLiteral() || destinationField.isLiteral()) {
            return "THERE ARE NO FIELD LITERALS";
        }

        StringBuilder jasminInstruction = new StringBuilder();
        Element newFieldValue = instruction.getThirdOperand();

        if (destinationObject.getType().getTypeOfElement() == ElementType.OBJECTREF || destinationObject.getType().getTypeOfElement() == ElementType.THIS) {
            jasminInstruction.append(getIndentation(indentationLevel)).append(getCorrespondingLoad(destinationObject, ancestorMethod)).append("\n");
            jasminInstruction.append(getIndentation(indentationLevel)).append(getCorrespondingLoad(newFieldValue, ancestorMethod)).append("\n");
            jasminInstruction.append(getIndentation(indentationLevel)).append("putfield ");
        } else {
            jasminInstruction.append(getIndentation(indentationLevel)).append("putstatic ");
        }
        ClassType classType = (ClassType) destinationObject.getType();

        jasminInstruction.append(classType.getName()).append("/").append(((Operand) destinationField).getName());
        jasminInstruction.append(" ").append(JasminUtils.translateType(ancestorMethod.getOllirClass(), destinationField.getType()));

        return jasminInstruction.toString();
    }

    public String translateInstruction(AssignInstruction instruction, Method ancestorMethod, int indentationLevel) {
        Element destination = instruction.getDest();

        if (destination.isLiteral()) {
            return "UNABLE TO STORE TO A LITERAL";
        }

        Instruction rhs = instruction.getRhs();

        if (rhs.getInstType() == InstructionType.CALL) {
            if (((CallInstruction) rhs).getInvocationType() == CallType.NEW) {
                return translateInstruction(rhs, ancestorMethod, indentationLevel);
            }
        }

        return translateInstruction(rhs, ancestorMethod, indentationLevel) + "\n" + getIndentation(indentationLevel) + getCorrespondingStore(destination, ancestorMethod);
    }

    public String translateInstruction(CallInstruction instruction, Method ancestorMethod, int indentationLevel) {
        StringBuilder jasminInstruction = new StringBuilder();
        StringBuilder parametersDescriptor = new StringBuilder();
        Operand caller = (Operand) instruction.getFirstArg();
        LiteralElement methodName = (LiteralElement) instruction.getSecondArg();

        CallType callType = instruction.getInvocationType();

        switch (callType) {
            case invokestatic:
            case invokevirtual:
                if (callType == CallType.invokevirtual) {
                    jasminInstruction.append(getIndentation(indentationLevel)).append(getCorrespondingLoad(caller, ancestorMethod)).append("\n");
                }

                for (Element element: instruction.getListOfOperands()) {
                    jasminInstruction.append(getIndentation(indentationLevel)).append(getCorrespondingLoad(element, ancestorMethod)).append("\n");
                    parametersDescriptor.append(JasminUtils.translateType(ancestorMethod.getOllirClass(), element.getType()));
                }

                jasminInstruction.append(getIndentation(indentationLevel));

                if (callType == CallType.invokestatic) {
                    jasminInstruction.append("invokestatic ").append(caller.getName());
                } else {
                    jasminInstruction.append("invokevirtual ");

                    ClassType classType = (ClassType) instruction.getFirstArg().getType();
                    jasminInstruction.append(JasminUtils.getFullClassName(ancestorMethod.getOllirClass(), classType.getName()));
                }


                jasminInstruction.append(".").append(JasminUtils.trimLiteral(methodName.getLiteral()));
                jasminInstruction.append("(").append(parametersDescriptor);


                jasminInstruction.append(")").append(JasminUtils.translateType(ancestorMethod.getOllirClass(), instruction.getReturnType()));
                break;
            case invokespecial:
                if (ancestorMethod.isConstructMethod()) {
                    if (caller.getName().equals("this")) {
                        jasminInstruction.append(getIndentation(indentationLevel)).append(getCorrespondingLoad(caller, ancestorMethod)).append("\n");
                    }
                }

                for (Element element: instruction.getListOfOperands()) {
                    jasminInstruction.append(getIndentation(indentationLevel)).append(getCorrespondingLoad(element, ancestorMethod)).append("\n");
                    parametersDescriptor.append(JasminUtils.translateType(ancestorMethod.getOllirClass(), element.getType()));
                }

                jasminInstruction.append(getIndentation(indentationLevel));


                jasminInstruction.append("invokespecial ");

                if (ancestorMethod.isConstructMethod()) {
                    if (caller.getName().equals("this")) {
                        jasminInstruction.append(ancestorMethod.getOllirClass().getSuperClass());
                    }
                } else {
                    ClassType classType = (ClassType) instruction.getFirstArg().getType();
                    jasminInstruction.append(JasminUtils.getFullClassName(ancestorMethod.getOllirClass(), classType.getName()));
                }


                jasminInstruction.append(".").append(JasminUtils.trimLiteral(methodName.getLiteral()));
                jasminInstruction.append("(").append(parametersDescriptor);


                jasminInstruction.append(")").append(JasminUtils.translateType(ancestorMethod.getOllirClass(), instruction.getReturnType()));

                if (!ancestorMethod.isConstructMethod()) {
                    jasminInstruction.append("\n").append(getIndentation(indentationLevel)).append(getCorrespondingStore(instruction.getFirstArg(), ancestorMethod));
                }
                break;
            case NEW:
                ElementType elementType = caller.getType().getTypeOfElement();
                if (elementType == ElementType.OBJECTREF || elementType == ElementType.CLASS) {
                    jasminInstruction.append(getIndentation(indentationLevel)).append("new ").append(caller.getName()).append("\n");
                    jasminInstruction.append(getIndentation(indentationLevel)).append("dup");
                }
                break;
            case arraylength:
                break;
        }
        return jasminInstruction.toString();
    }

    public String translateInstruction(BinaryOpInstruction instruction, Method ancestorMethod, int indentationLevel) {
        Element first = instruction.getLeftOperand();
        Element second = instruction.getRightOperand();
        Operation operation = instruction.getOperation();
        OperationType operationType = operation.getOpType();
        StringBuilder jasminInstruction = new StringBuilder();

        switch (operationType) {
            case ADD:
            case ADDI32:
            case SUB:
            case SUBI32:
            case MUL:
            case MULI32:
            case DIV:
            case DIVI32:
            case LTH:
                if (first.getType().getTypeOfElement() != operation.getTypeInfo().getTypeOfElement() || second.getType().getTypeOfElement() != operation.getTypeInfo().getTypeOfElement()) {
                    return "UNMATCHING TYPES";
                }

                if (operation.getTypeInfo().getTypeOfElement() != ElementType.INT32) {
                    return "INCORRECT TYPE";
                }

                jasminInstruction.append(getIndentation(indentationLevel)).append(getCorrespondingLoad(first, ancestorMethod)).append("\n");
                jasminInstruction.append(getIndentation(indentationLevel)).append(getCorrespondingLoad(second, ancestorMethod)).append("\n");
                jasminInstruction.append(getIndentation(indentationLevel));

                if (operationType == OperationType.ADD || operationType == OperationType.ADDI32) {
                    jasminInstruction.append("iadd");
                } else if (operationType == OperationType.SUB || operationType == OperationType.SUBI32) {
                    jasminInstruction.append("isub");
                } else if (operationType == OperationType.MUL || operationType == OperationType.MULI32) {
                    jasminInstruction.append("imul");
                } else if (operationType == OperationType.DIV || operationType == OperationType.DIVI32){
                    jasminInstruction.append("idiv");
                } else {
                    jasminInstruction.append("if_cmplt");
                }

                return jasminInstruction.toString();
            case AND:
            case OR:
                if (first.getType().getTypeOfElement() != operation.getTypeInfo().getTypeOfElement() || second.getType().getTypeOfElement() != operation.getTypeInfo().getTypeOfElement()) {
                    return "UNMATCHING TYPES";
                }

                if (operation.getTypeInfo().getTypeOfElement() != ElementType.BOOLEAN) {
                    return "INCORRECT TYPE";
                }

                jasminInstruction.append(getIndentation(indentationLevel)).append(getCorrespondingLoad(first, ancestorMethod)).append("\n");
                jasminInstruction.append(getIndentation(indentationLevel)).append(getCorrespondingLoad(second, ancestorMethod)).append("\n");
                jasminInstruction.append(getIndentation(indentationLevel));

                if (operationType == OperationType.AND) {
                    jasminInstruction.append("iand");
                } else {
                    jasminInstruction.append("ior");
                }

                return jasminInstruction.toString();
        }
        return "";
    }

    public String translateInstruction(ReturnInstruction instruction, Method ancestorMethod, int indentationLevel) {
        StringBuilder jasminInstruction = new StringBuilder();
        ElementType returnType = instruction.getReturnType().getTypeOfElement();

        switch (returnType) {
            case BOOLEAN:
            case INT32:
            case OBJECTREF:
            case CLASS:
            case STRING:
                jasminInstruction.append(getIndentation(indentationLevel)).append(getCorrespondingLoad(instruction.getOperand(), ancestorMethod)).append("\n");

                jasminInstruction.append(getIndentation(indentationLevel));
                if (returnType == ElementType.BOOLEAN || returnType == ElementType.INT32) {
                    jasminInstruction.append("ireturn");
                } else {
                    jasminInstruction.append("areturn");
                }

                break;
            case VOID:
                jasminInstruction.append(getIndentation(indentationLevel)).append("return");
                break;
        }

        return jasminInstruction.toString();
    }

    private String getCorrespondingLoad(Element element, Method ancestorMethod) {
        if (element.isLiteral()) {
            LiteralElement literalElement = (LiteralElement) element;

            switch (literalElement.getType().getTypeOfElement()) {
                case INT32:
                case STRING:
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

    private String getCorrespondingStore(Element element, Method ancestorMethod) {
        if (element.isLiteral()) {
            return "UNABLE TO STORE TO A LITERAL";
        } else {
            Operand operand = (Operand) element;

            Descriptor operandDescriptor = ancestorMethod.getVarTable().get(operand.getName());

            switch (operand.getType().getTypeOfElement()) {
                case INT32:
                case BOOLEAN:
                    return "istore " + operandDescriptor.getVirtualReg();
                case CLASS:
                case OBJECTREF:
                case THIS:
                case STRING:
                    return "astore " + operandDescriptor.getVirtualReg();
                default:
                    return "";
            }
        }
    }

    private String getIndentation(int indentationLevel) {
        return "\t".repeat(indentationLevel);
    }
}
