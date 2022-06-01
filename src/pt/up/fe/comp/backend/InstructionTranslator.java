package pt.up.fe.comp.backend;

import org.specs.comp.ollir.*;

import java.util.ArrayList;

public class InstructionTranslator {
    private int indentationLevel = 1;
    private int labelCounter = 0;

    public String translateInstruction(Instruction instruction, Method ancestorMethod) {
        InstructionType instructionType = instruction.getInstType();

        switch (instructionType) {
            case CALL:
                return translateInstruction((CallInstruction) instruction, ancestorMethod);
            case RETURN:
                return translateInstruction((ReturnInstruction) instruction, ancestorMethod);
            case PUTFIELD:
                return translateInstruction((PutFieldInstruction) instruction, ancestorMethod);
            case GETFIELD:
                return translateInstruction((GetFieldInstruction) instruction, ancestorMethod);
            case ASSIGN:
                return translateInstruction((AssignInstruction) instruction, ancestorMethod);
            case BINARYOPER:
                return translateInstruction((BinaryOpInstruction) instruction, ancestorMethod);
            case UNARYOPER:
                return translateInstruction((UnaryOpInstruction) instruction, ancestorMethod);
            case NOPER:
                return translateInstruction((SingleOpInstruction) instruction, ancestorMethod);
            case GOTO:
                return translateInstruction((GotoInstruction) instruction, ancestorMethod);
            case BRANCH:
                return translateInstruction((CondBranchInstruction) instruction, ancestorMethod);
            default:
                return "";
        }
    }

    public String translateInstruction(CondBranchInstruction instruction, Method ancestorMethod) {
        StringBuilder jasminInstruction = new StringBuilder();
        jasminInstruction.append(translateInstruction(instruction.getCondition(), ancestorMethod)).append("\n");
        jasminInstruction.append(getIndentation()).append("ldc 1").append("\n");
        jasminInstruction.append(getIndentation()).append("ifeq ").append(instruction.getLabel());
        return jasminInstruction.toString();
    }

    public String translateInstruction(GotoInstruction instruction, Method ancestorMethod) {
        return getIndentation() + "goto " + instruction.getLabel();
    }

    public String translateInstruction(UnaryOpInstruction instruction, Method ancestorMethod) {
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

            jasminInstruction.append(getCorrespondingLoad(first, ancestorMethod)).append("\n");
            jasminInstruction.append(getIndentation()).append("ineg");

            return jasminInstruction.toString();
        }

        return "UNSUPPORTED UNARY OPERATION";
    }

    public String translateInstruction(SingleOpInstruction instruction, Method ancestorMethod) {
        return getCorrespondingLoad(instruction.getSingleOperand(), ancestorMethod);
    }

    public String translateInstruction(GetFieldInstruction instruction, Method ancestorMethod) {
        Element destinationObject = instruction.getFirstOperand();
        Element destinationField = instruction.getSecondOperand();

        if (destinationObject.isLiteral() || destinationField.isLiteral()) {
            return "THERE ARE NO FIELD LITERALS";
        }

        StringBuilder jasminInstruction = new StringBuilder();

        if (destinationObject.getType().getTypeOfElement() == ElementType.OBJECTREF || destinationObject.getType().getTypeOfElement() == ElementType.THIS) {
            jasminInstruction.append(getCorrespondingLoad(destinationObject, ancestorMethod)).append("\n");
            jasminInstruction.append(getIndentation()).append("getfield ");
        } else if (destinationObject.getType().getTypeOfElement() == ElementType.CLASS) {
            jasminInstruction.append(getIndentation()).append("getstatic ");
        }

        ClassType classType = (ClassType) destinationObject.getType();

        jasminInstruction.append(classType.getName()).append("/").append(((Operand) destinationField).getName());
        jasminInstruction.append(" ").append(JasminUtils.translateType(ancestorMethod.getOllirClass(), destinationField.getType()));

        return jasminInstruction.toString();
    }

    public String translateInstruction(PutFieldInstruction instruction, Method ancestorMethod) {
        Element destinationObject = instruction.getFirstOperand();
        Element destinationField = instruction.getSecondOperand();

        if (destinationObject.isLiteral() || destinationField.isLiteral()) {
            return "THERE ARE NO FIELD LITERALS";
        }

        StringBuilder jasminInstruction = new StringBuilder();
        Element newFieldValue = instruction.getThirdOperand();

        if (destinationObject.getType().getTypeOfElement() == ElementType.OBJECTREF || destinationObject.getType().getTypeOfElement() == ElementType.THIS) {
            jasminInstruction.append(getCorrespondingLoad(destinationObject, ancestorMethod)).append("\n");
            jasminInstruction.append(getCorrespondingLoad(newFieldValue, ancestorMethod)).append("\n");
            jasminInstruction.append(getIndentation()).append("putfield ");
        } else {
            jasminInstruction.append(getIndentation()).append("putstatic ");
        }
        ClassType classType = (ClassType) destinationObject.getType();

        jasminInstruction.append(classType.getName()).append("/").append(((Operand) destinationField).getName());
        jasminInstruction.append(" ").append(JasminUtils.translateType(ancestorMethod.getOllirClass(), destinationField.getType()));

        return jasminInstruction.toString();
    }

    public String translateInstruction(AssignInstruction instruction, Method ancestorMethod) {
        Element destination = instruction.getDest();

        if (destination.isLiteral()) {
            return "UNABLE TO STORE TO A LITERAL";
        }

        Instruction rhs = instruction.getRhs();

        if (destination instanceof ArrayOperand) {
            return getCorrespondingStore(destination, ancestorMethod) + "\n" + translateInstruction(rhs, ancestorMethod) + "\n" + getIndentation() + "aastore";
        }

        return translateInstruction(rhs, ancestorMethod) + "\n" + getCorrespondingStore(destination, ancestorMethod);
    }

    public String translateInstruction(CallInstruction instruction, Method ancestorMethod) {
        StringBuilder jasminInstruction = new StringBuilder();
        StringBuilder parametersDescriptor = new StringBuilder();
        Operand caller = (Operand) instruction.getFirstArg();
        LiteralElement methodName = (LiteralElement) instruction.getSecondArg();

        CallType callType = instruction.getInvocationType();

        switch (callType) {
            case invokestatic:
            case invokevirtual:
                if (callType == CallType.invokevirtual) {
                    jasminInstruction.append(getCorrespondingLoad(caller, ancestorMethod)).append("\n");
                }

                for (Element element: instruction.getListOfOperands()) {
                    jasminInstruction.append(getCorrespondingLoad(element, ancestorMethod)).append("\n");
                    parametersDescriptor.append(JasminUtils.translateType(ancestorMethod.getOllirClass(), element.getType()));
                }

                jasminInstruction.append(getIndentation());

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
                        jasminInstruction.append(getCorrespondingLoad(caller, ancestorMethod)).append("\n");
                    }
                }

                for (Element element: instruction.getListOfOperands()) {
                    jasminInstruction.append(getCorrespondingLoad(element, ancestorMethod)).append("\n");
                    parametersDescriptor.append(JasminUtils.translateType(ancestorMethod.getOllirClass(), element.getType()));
                }

                jasminInstruction.append(getIndentation());


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
                    jasminInstruction.append("\n").append(getCorrespondingStore(instruction.getFirstArg(), ancestorMethod));
                }
                break;
            case NEW:
                ElementType elementType = caller.getType().getTypeOfElement();
                if (elementType == ElementType.OBJECTREF || elementType == ElementType.CLASS) {
                    jasminInstruction.append(getIndentation()).append("new ").append(caller.getName()).append("\n");
                    jasminInstruction.append(getIndentation()).append("dup");
                } else if (elementType == ElementType.ARRAYREF) {
                    ArrayList<Element> operands = instruction.getListOfOperands();
                    if (operands.size() < 1) {
                        return "";
                    }

                    jasminInstruction.append(getCorrespondingLoad(operands.get(0), ancestorMethod)).append("\n");
                    jasminInstruction.append(getIndentation()).append("newarray int");
                }
                break;
            case arraylength:
                jasminInstruction.append(getCorrespondingLoad(caller, ancestorMethod)).append("\n");
                jasminInstruction.append(getIndentation()).append("arraylength");
                break;
        }
        return jasminInstruction.toString();
    }

    public String translateInstruction(BinaryOpInstruction instruction, Method ancestorMethod) {
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

                jasminInstruction.append(getCorrespondingLoad(first, ancestorMethod)).append("\n");
                jasminInstruction.append(getCorrespondingLoad(second, ancestorMethod)).append("\n");
                jasminInstruction.append(getIndentation());

                if (operationType == OperationType.ADD || operationType == OperationType.ADDI32) {
                    jasminInstruction.append("iadd");
                } else if (operationType == OperationType.SUB || operationType == OperationType.SUBI32) {
                    jasminInstruction.append("isub");
                } else if (operationType == OperationType.MUL || operationType == OperationType.MULI32) {
                    jasminInstruction.append("imul");
                } else if (operationType == OperationType.DIV || operationType == OperationType.DIVI32){
                    jasminInstruction.append("idiv");
                } else {
                    jasminInstruction.append(this.getIfBody("if_cmplt"));
                }

                return jasminInstruction.toString();
            case AND:
            case ANDB:
            case OR:
            case ORB:
                if (first.getType().getTypeOfElement() != operation.getTypeInfo().getTypeOfElement() || second.getType().getTypeOfElement() != operation.getTypeInfo().getTypeOfElement()) {
                    return "UNMATCHING TYPES";
                }

                if (operation.getTypeInfo().getTypeOfElement() != ElementType.BOOLEAN) {
                    return "INCORRECT TYPE";
                }

                jasminInstruction.append(getCorrespondingLoad(first, ancestorMethod)).append("\n");
                jasminInstruction.append(getCorrespondingLoad(second, ancestorMethod)).append("\n");
                jasminInstruction.append(getIndentation());

                if (operationType == OperationType.AND || operationType == OperationType.ANDB) {
                    jasminInstruction.append("iand");
                } else {
                    jasminInstruction.append("ior");
                }

                return jasminInstruction.toString();
            case EQ:
            case EQI32:
                jasminInstruction.append(getCorrespondingLoad(first, ancestorMethod)).append("\n");
                jasminInstruction.append(getCorrespondingLoad(second, ancestorMethod)).append("\n");
                jasminInstruction.append(getIndentation());

                jasminInstruction.append(this.getIfBody("ifeq"));

                return jasminInstruction.toString();
        }
        return "";
    }

    public String translateInstruction(ReturnInstruction instruction, Method ancestorMethod) {
        StringBuilder jasminInstruction = new StringBuilder();
        ElementType returnType = instruction.getReturnType().getTypeOfElement();

        switch (returnType) {
            case BOOLEAN:
            case INT32:
            case OBJECTREF:
            case CLASS:
            case STRING:
            case ARRAYREF:
                jasminInstruction.append(getCorrespondingLoad(instruction.getOperand(), ancestorMethod)).append("\n");

                jasminInstruction.append(getIndentation());
                if (returnType == ElementType.BOOLEAN || returnType == ElementType.INT32) {
                    jasminInstruction.append("ireturn");
                } else {
                    jasminInstruction.append("areturn");
                }

                break;
            case VOID:
                jasminInstruction.append(getIndentation()).append("return");
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
                    return getIndentation() + "ldc " + JasminUtils.trimLiteral(literalElement.getLiteral());
                case BOOLEAN:
                    String literal = JasminUtils.trimLiteral(literalElement.getLiteral());
                    return getIndentation() + (literal.equals("true") || literal.equals("1") ? "ldc 1" : "ldc 0");
                default:
                    return "Literal";
            }
        } else {
            Operand operand = (Operand) element;

            Descriptor operandDescriptor = ancestorMethod.getVarTable().get(operand.getName());

            switch (operandDescriptor.getVarType().getTypeOfElement()) {
                case INT32:
                case BOOLEAN:
                    return getIndentation() + "iload " + operandDescriptor.getVirtualReg();
                case ARRAYREF:
                    StringBuilder jasminInstruction = new StringBuilder();

                    jasminInstruction.append(getIndentation()).append("aload ").append(operandDescriptor.getVirtualReg());

                    if (element instanceof ArrayOperand) {
                        ArrayOperand arrayOperand = (ArrayOperand) operand;

                        jasminInstruction.append("\n");

                        ArrayList<Element> indexes = arrayOperand.getIndexOperands();

                        if (indexes.size() < 1) {
                            return "";
                        }

                        Element index = indexes.get(0);
                        jasminInstruction.append(getCorrespondingLoad(index, ancestorMethod)).append("\n");

                        jasminInstruction.append(getIndentation()).append("aaload");
                    }

                    return jasminInstruction.toString();
                case CLASS:
                case OBJECTREF:
                case THIS:
                case STRING:
                    return getIndentation() + "aload " + operandDescriptor.getVirtualReg();
                default:
                    return "Hello";
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
                    if (element instanceof ArrayOperand) {
                        ArrayOperand arrayOperand = (ArrayOperand) operand;
                        StringBuilder jasminInstruction = new StringBuilder();

                        jasminInstruction.append(getIndentation()).append("aload ").append(operandDescriptor.getVirtualReg()).append("\n");

                        ArrayList<Element> indexes = arrayOperand.getIndexOperands();

                        if (indexes.size() < 1) {
                            return "";
                        }

                        Element index = indexes.get(0);
                        jasminInstruction.append(getCorrespondingLoad(index, ancestorMethod));

                        return jasminInstruction.toString();
                    }

                    return getIndentation() + "istore " + operandDescriptor.getVirtualReg();
                case CLASS:
                case OBJECTREF:
                case THIS:
                case STRING:
                    return getIndentation() + "astore " + operandDescriptor.getVirtualReg();
                case ARRAYREF:
                    StringBuilder jasminInstruction = new StringBuilder();

                    if (element instanceof ArrayOperand) {
                        ArrayOperand arrayOperand = (ArrayOperand) operand;
                        jasminInstruction.append(getIndentation()).append("aload ").append(operandDescriptor.getVirtualReg()).append("\n");

                        ArrayList<Element> indexes = arrayOperand.getIndexOperands();

                        if (indexes.size() < 1) {
                            return "";
                        }

                        Element index = indexes.get(0);
                        jasminInstruction.append(getCorrespondingLoad(index, ancestorMethod)).append("\n");
                    } else {
                        jasminInstruction.append(getIndentation()).append("astore ").append(operandDescriptor.getVirtualReg());
                    }

                    return jasminInstruction.toString();
                default:
                    return "";
            }
        }
    }

    private String getIfBody(String comparisonInstruction) {
        StringBuilder ifBody = new StringBuilder();
        ifBody.append(comparisonInstruction).append(" Then").append(this.labelCounter).append("\n");
        ifBody.append(getIndentation()).append("ldc 0").append("\n");
        ifBody.append(getIndentation()).append("goto Finally").append(this.labelCounter).append("\n");
        this.indentationLevel--;
        ifBody.append(getIndentation()).append("Then").append(this.labelCounter).append(":").append("\n");
        this.indentationLevel++;

        ifBody.append(getIndentation()).append("ldc 1").append("\n");

        this.indentationLevel--;
        ifBody.append(getIndentation()).append("Finally").append(this.labelCounter).append(":");
        this.indentationLevel++;
        this.labelCounter++;

        return ifBody.toString();
    }

    private String getIndentation() {
        return "\t".repeat(this.indentationLevel);
    }
}
