package pt.up.fe.comp.jmm.ollir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.specs.comp.ollir.ClassUnit;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportsProvider;
import pt.up.fe.specs.util.SpecsCollections;

/**
 * An OLLIR result returns the parsed OLLIR code and the corresponding symbol table.
 */
public class OllirResult implements ReportsProvider {

    private final String ollirCode;
    private final ClassUnit ollirClass;
    private final SymbolTable symbolTable;
    private final List<Report> reports;
    private final Map<String, String> config;

    private OllirResult(String ollirCode, ClassUnit ollirClass, SymbolTable symbolTable, List<Report> reports,
            Map<String, String> config) {

        this.ollirCode = ollirCode;
        this.ollirClass = ollirClass;
        this.symbolTable = symbolTable;
        this.reports = reports;
        this.config = config;
    }

    public OllirResult(String ollirCode, Map<String, String> config) {
        this.ollirCode = ollirCode;
        this.ollirClass = OllirUtils.parse(ollirCode);
        this.symbolTable = null;
        this.reports = new ArrayList<>();
        this.config = config;
    }

    /**
     * Creates a new instance from the analysis stage results and a String containing OLLIR code.
     * 
     * @param semanticsResult
     * @param ollirCode
     * @param reports
     */
    public OllirResult(JmmSemanticsResult semanticsResult, String ollirCode, List<Report> reports) {
        this(ollirCode, OllirUtils.parse(ollirCode), semanticsResult.getSymbolTable(),
                SpecsCollections.concat(semanticsResult.getReports(), reports), semanticsResult.getConfig());
    }

    public String getOllirCode() {
        return ollirCode;
    }

    public ClassUnit getOllirClass() {
        return this.ollirClass;
    }

    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    @Override
    public List<Report> getReports() {
        return this.reports;
    }

    public Map<String, String> getConfig() {
        return config;
    }
}
