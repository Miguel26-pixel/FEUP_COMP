package pt.up.fe.comp.semantic.visitors;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public abstract class ReportCollectorJmmNodeVisitor<D, R> extends AJmmVisitor<D, R> {
    protected List<Report> reports = new ArrayList<>();

    public List<Report> getReports() {
        return reports;
    }

    public void addSemanticErrorReport(int line, String message) {
        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, line, message));
    }
}
