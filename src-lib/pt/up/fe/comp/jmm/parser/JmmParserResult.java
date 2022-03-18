package pt.up.fe.comp.jmm.parser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmSerializer;
import pt.up.fe.comp.jmm.report.Report;

public class JmmParserResult {

    private final JmmNode rootNode;
    private final List<Report> reports;
    private final Map<String, String> config;

    public JmmParserResult(JmmNode rootNode, List<Report> reports, Map<String, String> config) {
        this.rootNode = rootNode != null ? rootNode.sanitize() : null;
        this.reports = reports;
        this.config = config;
    }

    /**
     * Utility constructor when an error occurs.
     * 
     * @param errorReport
     */
    // public JmmParserResult(Report errorReport) {
    // this(null, Arrays.asList(errorReport), Collections.emptyMap());
    // }

    public static JmmParserResult newError(Report errorReport) {
        return new JmmParserResult(null, Arrays.asList(errorReport), Collections.emptyMap());
    }

    public JmmNode getRootNode() {
        return this.rootNode;
    }

    public List<Report> getReports() {
        return this.reports;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public String toJson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                // .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(JmmNode.class, new JmmSerializer())
                .create();
        return gson.toJson(this, JmmParserResult.class);
    }
}
