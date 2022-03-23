package pt.up.fe.comp.jmm.analysis;

import java.util.List;
import java.util.Map;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportsProvider;
import pt.up.fe.specs.util.SpecsCollections;

/**
 * A semantic analysis returns the analysed tree and the generated symbol table.
 */
public class JmmSemanticsResult implements ReportsProvider {

    private final JmmNode rootNode;
    private final SymbolTable symbolTable;
    private final List<Report> reports;
    private final Map<String, String> config;

    public JmmSemanticsResult(JmmNode rootNode, SymbolTable symbolTable, List<Report> reports,
            Map<String, String> config) {
        this.rootNode = rootNode;
        this.symbolTable = symbolTable;
        this.reports = reports;
        this.config = config;
    }

    public JmmSemanticsResult(JmmParserResult parserResult, SymbolTable symbolTable, List<Report> reports) {
        this(parserResult.getRootNode(), symbolTable, SpecsCollections.concat(parserResult.getReports(), reports),
                parserResult.getConfig());
    }

    public JmmNode getRootNode() {
        return this.rootNode;
    }

    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    public List<Report> getReports() {
        return this.reports;
    }

    public Map<String, String> getConfig() {
        return config;
    }
}
