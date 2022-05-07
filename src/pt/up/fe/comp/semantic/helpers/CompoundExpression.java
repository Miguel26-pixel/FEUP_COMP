package pt.up.fe.comp.semantic.helpers;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.semantic.tables.JmmSymbolTable;

import java.util.Optional;

public class CompoundExpression {
    JmmSymbolTable symbolTable;
    String firstOpType;
    Boolean firstOpIsArray;
    String secondOpType;
    Boolean secondOpIsArray;
    Boolean indexTypeError = false;


    public CompoundExpression(JmmNode compound, JmmSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.firstOpType = this.setFirstOPType(compound);
        this.secondOpType = this.setSecondOPType(compound);
        if (!secondOpType.equals("int") || secondOpIsArray) {
            indexTypeError = true;
        }
    }

    public Boolean getIndexTypeError() {
        return indexTypeError;
    }

    public Boolean getFirstOpIsArray() {
        return firstOpIsArray;
    }

    public Boolean getSecondOpIsArray() {
        return secondOpIsArray;
    }

    public String getSecondOpType() {
        return secondOpType;
    }

    public String getFirstOpType() {
        return firstOpType;
    }

    private String setFirstOPType(JmmNode compound) {
        String kind = compound.getChildren().get(0).getKind();
        switch (kind) {
            case "Identifier":
                Optional<Symbol> symbol = this.symbolTable.getClosestSymbol(compound,compound.getChildren().get(0).get("name"));
                if (symbol.isPresent()) {
                    this.firstOpIsArray = symbol.get().getType().isArray();
                    return symbol.get().getType().getName();
                }
                break;
            default: break;
        }
        return "";
    }

    private String setSecondOPType(JmmNode compound) {
        String kind = compound.getChildren().get(0).getKind();
        switch (kind) {
            case "Indexation":
                String indexKind = compound.getChildren().get(0).getChildren().get(0).getKind();
                switch (indexKind) {
                    case "BinOp":
                        Optional<Symbol> symbol = this.symbolTable.getClosestSymbol(compound, compound.getChildren().get(0).get("name"));
                        if (symbol.isPresent()) {
                            this.firstOpIsArray = symbol.get().getType().isArray();
                            return symbol.get().getType().getName();
                        }
                        break;
                    case "IntLiteral":
                        this.secondOpIsArray = false;
                        return "int";
                    case "BooleanLiteral":
                        this.secondOpIsArray = false;
                        return "bool";
                    default:
                        break;
                }
        }
        return "";
    }
}
