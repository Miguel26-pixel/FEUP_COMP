package pt.up.fe.comp.backend;

import org.specs.comp.ollir.*;

import java.util.ArrayList;

public class InstructionTranslator {
    private int indentationLevel = 1;
    private int labelCounter = 0;
    private int loadCounter = 1;
    private int maxLoadCounter = 1;

    public String translateInstruction(Instruction instruction, Method ancestorMethod) {
        InstructionType instructionType = instruction.getInstType();
        StringBuilder translatedInstruction = new StringBuilder();
        loadCounter = 1;

        switch (instructionType) {
            case CALL:
                translatedInstruction.append(translateInstruction((CallInstruction) instruction, ancestorMethod));
                break;
            case RETURN:
                translatedInstruction.append(translateInstruction((ReturnInstruction) instruction, ancestorMethod));
                break;
            case PUTFIELD:
                translatedInstruction.append(translateInstruction((PutFieldInstruction) instruction, ancestorMethod));
                break;
            case GETFIELD:
                translatedInstruction.append(translateInstruction((GetFieldInstruction) instruction, ancestorMethod));
                break;
            case ASSIGN:
                translatedInstruction.append(translateInstruction((AssignInstruction) instruction, ancestorMethod));
                break;
            case BINARYOPER:
                translatedInstruction.append(translateInstruction((BinaryOpInstruction) instruction, ancestorMethod));
                break;
            case UNARYOPER:
                translatedInstruction.append(translateInstruction((UnaryOpInstruction) instruction, ancestorMethod));
                break;
            case NOPER:
                translatedInstruction.append(translateInstruction((SingleOpInstruction) instruction, ancestorMethod));
                break;
            case GOTO:
                translatedInstruction.append(translateInstruction((GotoInstruction) instruction));
                break;
            case BRANCH:
                translatedInstruction.append(translateInstruction((CondBranchInstruction) instruction, ancestorMethod));
                break;
            default:
                break;
        }

        this.maxLoadCounter = Integer.max(this.maxLoadCounter, this.loadCounter);

        return translatedInstruction.toString();
    }

    public String translateInstruction(CondBranchInstruction instruction, Method ancestorMethod) {
        return translateInstruction(instruction.getCondition(), ancestorMethod) + "\n" +
                getIndentation() + "ifne " + instruction.getLabel();
    }

    public String translateInstruction(GotoInstruction instruction) {
        return getIndentation() + "goto " + instruction.getLabel();
    }

    public String translateInstruction(UnaryOpInstruction instruction, Method ancestorMethod) {
        Operation operation = instruction.getOperation();
        OperationType operationType = operation.getOpType();
        Element first = instruction.getOperand();

        if (operationType == OperationType.NOT || operationType == OperationType.NOTB) {
            return getCorrespondingLoad(first, ancestorMethod) + "\n" +
                    getIndentation() + getIfBody("ifeq");
        }

        return "";
    }

    public String translateInstruction(SingleOpInstruction instruction, Method ancestorMethod) {
        return getCorrespondingLoad(instruction.getSingleOperand(), ancestorMethod);
    }

    public String translateInstruction(GetFieldInstruction instruction, Method ancestorMethod) {
        Element destinationObject = instruction.getFirstOperand();
        Element destinationField = instruction.getSecondOperand();

        if (destinationObject.isLiteral() || destinationField.isLiteral()) {
            return "";
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
            return "";
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
            return "";
        }

        Instruction rhs = instruction.getRhs();

        if (destination instanceof ArrayOperand) {
            return getCorrespondingStore(destination, ancestorMethod) + "\n" + translateInstruction(rhs, ancestorMethod) + "\n" + getIndentation() + "iastore";
        }

        if (rhs.getInstType() == InstructionType.CALL) {
            CallInstruction callInstruction = (CallInstruction) rhs;
            if (callInstruction.getInvocationType() == CallType.NEW) {
                ElementType elementType = callInstruction.getFirstArg().getType().getTypeOfElement();
                if (elementType != ElementType.ARRAYREF) {
                    return translateInstruction(rhs, ancestorMethod);
                }
            }
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

                for (Element element : instruction.getListOfOperands()) {
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

                for (Element element : instruction.getListOfOperands()) {
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
            case SUB:
            case MUL:
            case DIV:
            case LTH:
            case AND:
            case ANDB:
            case OR:
            case ORB:
            case EQ:
                String operationString;
                String loads = getCorrespondingLoad(first, ancestorMethod) + "\n"
                        + getCorrespondingLoad(second, ancestorMethod) + "\n";

                if (operationType == OperationType.ADD) {
                    if (!first.isLiteral() && second.isLiteral()) {
                        return getIinc(ancestorMethod, (LiteralElement) second, (Operand) first, jasminInstruction);
                    } else if (first.isLiteral() && !second.isLiteral()) {
                        return getIinc(ancestorMethod, (LiteralElement) first, (Operand) second, jasminInstruction);
                    } else {
                        operationString = "iadd";
                    }
                } else if (operationType == OperationType.SUB) {
                    operationString = "isub";
                } else if (operationType == OperationType.MUL) {
                    operationString = "imul";
                } else if (operationType == OperationType.DIV) {
                    operationString = "idiv";
                } else if (operationType == OperationType.LTH) {
                    try {
                        if (second.isLiteral()) {
                            LiteralElement literalElement = (LiteralElement) second;

                            int literal = Integer.parseInt(JasminUtils.trimLiteral(literalElement.getLiteral()));
                            if (literal == 0) {
                                return getCorrespondingLoad(first, ancestorMethod) + "\n" + getIndentation() + this.getIfBody("iflt");
                            } else {
                                throw new Exception("");
                            }
                        } else {
                            throw new Exception("");
                        }
                    } catch (Exception e) {
                        operationString = this.getIfBody("if_icmplt");
                    }
                } else if (operationType == OperationType.AND || operationType == OperationType.ANDB) {
                    operationString = "iand";
                } else if (operationType == OperationType.OR || operationType == OperationType.ORB) {
                    operationString = "ior";
                } else {
                    operationString = this.getIfBody("if_icmpeq");
                }

                return loads + getIndentation() + operationString;
        }
        return "";
    }

    private String getIinc(Method ancestorMethod, LiteralElement literalElement, Operand operand, StringBuilder jasminInstruction) {
        jasminInstruction.append("iinc ").append(this.getVirtualReg(operand, ancestorMethod));
        jasminInstruction.append(" ");

        jasminInstruction.append(JasminUtils.trimLiteral(literalElement.getLiteral()));
        return getIndentation() + jasminInstruction + "\n" + getCorrespondingLoad(operand, ancestorMethod);
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
        this.loadCounter += 1;
        if (element.isLiteral()) {
            LiteralElement literalElement = (LiteralElement) element;

            switch (literalElement.getType().getTypeOfElement()) {
                case INT32:
                case BOOLEAN:
                    StringBuilder jasminInstruction = new StringBuilder(getIndentation());
                    String literal = JasminUtils.trimLiteral(literalElement.getLiteral());

                    try {
                        int literalInt = Integer.parseInt(literal);

                        if (literalInt <= 5) {
                            jasminInstruction.append("iconst_").append(literal);
                        } else if (literalInt < Math.pow(2, 7)) {
                            jasminInstruction.append("bipush ").append(literal);
                        } else if (literalInt < Math.pow(2, 15)) {
                            jasminInstruction.append("sipush ").append(literal);
                        } else {
                            throw new Exception("");
                        }
                    } catch (Exception e) {
                        jasminInstruction.append("ldc ").append(literal);
                    }
                    return jasminInstruction.toString();
                default:
                    return "";
            }
        } else {
            Operand operand = (Operand) element;

            Descriptor operandDescriptor = ancestorMethod.getVarTable().get(operand.getName());
            if (operandDescriptor.getVirtualReg() < 0) {
                return "";
            }

            String spacer = operandDescriptor.getVirtualReg() < 4 ? "_" : " ";

            switch (operandDescriptor.getVarType().getTypeOfElement()) {
                case INT32:
                case BOOLEAN:
                    return getIndentation() + "iload" + spacer + operandDescriptor.getVirtualReg();
                case ARRAYREF:
                    StringBuilder jasminInstruction = new StringBuilder();

                    jasminInstruction.append(getIndentation()).append("aload").append(spacer).append(operandDescriptor.getVirtualReg());

                    if (element instanceof ArrayOperand) {
                        ArrayOperand arrayOperand = (ArrayOperand) operand;

                        jasminInstruction.append("\n");

                        ArrayList<Element> indexes = arrayOperand.getIndexOperands();

                        if (indexes.size() < 1) {
                            return "";
                        }

                        Element index = indexes.get(0);
                        jasminInstruction.append(getCorrespondingLoad(index, ancestorMethod)).append("\n");

                        jasminInstruction.append(getIndentation()).append("iaload");
                    }

                    return jasminInstruction.toString();
                case CLASS:
                case OBJECTREF:
                case THIS:
                case STRING:
                    return getIndentation() + "aload" + spacer + operandDescriptor.getVirtualReg();
                default:
                    return "";
            }
        }
    }

    private String getCorrespondingStore(Element element, Method ancestorMethod) {
        if (element.isLiteral()) {
            return "";
        } else {
            Operand operand = (Operand) element;

            Descriptor operandDescriptor = ancestorMethod.getVarTable().get(operand.getName());

            String spacer = operandDescriptor.getVirtualReg() < 4 ? "_" : " ";

            switch (operand.getType().getTypeOfElement()) {
                case INT32:
                case BOOLEAN:
                    if (element instanceof ArrayOperand) {
                        ArrayOperand arrayOperand = (ArrayOperand) operand;
                        StringBuilder jasminInstruction = new StringBuilder();
                        jasminInstruction.append(getIndentation()).append("aload").append(spacer).append(operandDescriptor.getVirtualReg()).append("\n");

                        ArrayList<Element> indexes = arrayOperand.getIndexOperands();
                        Element index = indexes.get(0);

                        jasminInstruction.append(getCorrespondingLoad(index, ancestorMethod));
                        return jasminInstruction.toString();
                    }

                    return getIndentation() + "istore" + spacer + operandDescriptor.getVirtualReg();
                case CLASS:
                case OBJECTREF:
                case THIS:
                case STRING:
                    return getIndentation() + "astore" + spacer + operandDescriptor.getVirtualReg();
                case ARRAYREF:
                    StringBuilder jasminInstruction = new StringBuilder();

                    if (element instanceof ArrayOperand) {
                        ArrayOperand arrayOperand = (ArrayOperand) operand;
                        jasminInstruction.append(getIndentation()).append("aload").append(spacer).append(operandDescriptor.getVirtualReg()).append("\n");

                        ArrayList<Element> indexes = arrayOperand.getIndexOperands();
                        Element index = indexes.get(0);

                        jasminInstruction.append(getCorrespondingLoad(index, ancestorMethod)).append("\n");
                    } else {
                        jasminInstruction.append(getIndentation()).append("astore").append(spacer).append(operandDescriptor.getVirtualReg());
                    }

                    return jasminInstruction.toString();
                default:
                    return "";
            }
        }
    }

    private String getVirtualReg(Operand operand, Method ancestorMethod) {
        Descriptor operandDescriptor = ancestorMethod.getVarTable().get(operand.getName());

        return Integer.toString(operandDescriptor.getVirtualReg());
    }

    private String getIfBody(String comparisonInstruction) {
        StringBuilder ifBody = new StringBuilder();
        ifBody.append(comparisonInstruction).append(" Then_").append(this.labelCounter).append("\n");
        ifBody.append(getIndentation()).append("ldc 0").append("\n");
        ifBody.append(getIndentation()).append("goto Finally_").append(this.labelCounter).append("\n");
        this.indentationLevel--;
        ifBody.append(getIndentation()).append("Then_").append(this.labelCounter).append(":").append("\n");
        this.indentationLevel++;

        ifBody.append(getIndentation()).append("ldc 1").append("\n");

        this.indentationLevel--;
        ifBody.append(getIndentation()).append("Finally_").append(this.labelCounter).append(":");
        this.indentationLevel++;
        this.labelCounter++;

        return ifBody.toString();
    }

    private String getIndentation() {
        return "\t".repeat(this.indentationLevel);
    }

    public int getMaxLoadCounter() {
        return maxLoadCounter;
    }
}
