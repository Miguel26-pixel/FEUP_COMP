package pt.up.fe.comp.semantic.visitors;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

public abstract class ReportCollectorJmmNodeVisitor<D, R> extends AJmmVisitor<D, R> {
    protected List<Report> reports = new ArrayList<>();

    public List<Report> getReports() {
        return reports;
    }
}
