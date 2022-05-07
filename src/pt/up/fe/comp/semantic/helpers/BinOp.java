package pt.up.fe.comp.semantic.helpers;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;

import java.util.List;
import java.util.Optional;

public class BinOp {
    String binopType;
    String firstOpType;
    Boolean firstOpIsArray;
    String secondOpType;
    Boolean secondOpIsArray;
    JmmSymbolTable symbolTable;
    Boolean operationTypeError = false;
    Boolean assignTypeError = false;
    Boolean arrayOperationError = false;
    Boolean indexTypeError = false;

    public BinOp(JmmNode binop, JmmSymbolTable symbolTable) {
        System.out.println("tou");
        this.binopType = binop.get("op");
        this.symbolTable = symbolTable;
        this.firstOpType = this.setOPType(binop, 0);
        this.secondOpType = this.setOPType(binop, 1);
        this.checkErrors();
    }

    public String getFirstOpType() {
        return firstOpType;
    }

    public Boolean getFirstOpIsArray() {
        return firstOpIsArray;
    }

    public String getSecondOpType() {
        return secondOpType;
    }

    public Boolean getSecondOpIsArray() {
        return secondOpIsArray;
    }

    public Boolean getOperationTypeError() {
        return operationTypeError;
    }

    public Boolean getAssignTypeError() {
        return assignTypeError;
    }

    public Boolean getArrayOperationError() {
        return arrayOperationError;
    }

    public Boolean hasError() {
        return operationTypeError || assignTypeError || arrayOperationError;
    }

    private void checkErrors() {
        if (this.binopType.equals("or") || this.binopType.equals("and") || this.binopType.equals("lt")) {
            if (!this.firstOpType.equals("bool") || !this.secondOpType.equals("bool")) {
                this.operationTypeError = true;
            }
            if (this.firstOpIsArray || this.secondOpIsArray) {
                this.arrayOperationError = true;
            }
        } else if (this.binopType.equals("assign")) {
            if (!this.firstOpType.equals(this.secondOpType)) {
                this.assignTypeError = true;
            }
        } else {
            if (!this.firstOpType.equals("int") || !this.secondOpType.equals("int")) {
                this.operationTypeError = true;
            }
            if (this.firstOpIsArray || this.secondOpIsArray) {
                this.arrayOperationError = true;
            }
        }
    }

    private String setOPType(JmmNode binop, int index) {
        String kind = binop.getChildren().get(index).getKind();
        System.out.println("ola");
        switch (kind) {
            case "CompoundExpression":
                System.out.println("entrei compound");
                CompoundExpression compound = new CompoundExpression(binop.getChildren().get(index), this.symbolTable);
                this.indexTypeError = compound.indexTypeError;
                if (index == 0) {
                    this.firstOpIsArray = false;
                } else {
                    this.secondOpIsArray = false;
                }
                return compound.getFirstOpType();
            case "BinOp":
                BinOp b = new BinOp(binop.getChildren().get(index), this.symbolTable);
                if (b.hasError()) {
                    this.operationTypeError = b.getOperationTypeError();
                    this.assignTypeError = b.getAssignTypeError();
                    this.arrayOperationError = b.getArrayOperationError();
                }
                if (index == 0) {
                    this.firstOpIsArray = b.getFirstOpIsArray();
                    return b.getFirstOpType();
                } else {
                    this.secondOpIsArray = b.getSecondOpIsArray();
                    return b.getFirstOpType();
                }
            case "Identifier":
                Optional<Symbol> symbol = this.symbolTable.getClosestSymbol(binop,binop.getChildren().get(index).get("name"));
                if (symbol.isPresent()) {
                    if (index == 0) {
                        this.firstOpIsArray = symbol.get().getType().isArray();
                    } else {
                        this.secondOpIsArray = symbol.get().getType().isArray();
                    }
                    return symbol.get().getType().getName();
                }
                break;
            case "IntLiteral":
                if (index == 0) {
                    this.firstOpIsArray = false;
                } else {
                    this.secondOpIsArray = false;
                }
                return "int";
            case "BooleanLiteral":
                if (index == 0) {
                    this.firstOpIsArray = false;
                } else {
                    this.secondOpIsArray = false;
                }
                return "bool";
            default: break;
        }
        return "";
    }
}
