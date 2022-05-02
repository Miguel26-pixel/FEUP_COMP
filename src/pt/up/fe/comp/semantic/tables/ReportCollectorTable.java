package pt.up.fe.comp.semantic.tables;

import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

public abstract class ReportCollectorTable {
    protected final List<Report> reports = new ArrayList<>();

    public List<Report> getReports() {
        return reports;
    }
}
