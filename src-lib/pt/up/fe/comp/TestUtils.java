package pt.up.fe.comp;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast2jasmin.AstToJasmin;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParser;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.ReportsProvider;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;

public class TestUtils {

    private static final Properties CONFIG = TestUtils.loadProperties("config.properties");

    public static Properties loadProperties(String filename) {
        try {
            Properties props = new Properties();
            props.load(new StringReader(SpecsIo.read(filename)));
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Error while loading properties file '" + filename + "'", e);
        }
    }

    public static JmmParserResult parse(String code, String startingRule) {
        return parse(code, startingRule, Collections.emptyMap());
    }

    public static JmmParserResult parse(String code, String startingRule, Map<String, String> config) {
        JmmParser parser = getJmmParser();

        return parser.parse(code, startingRule, config);
    }

    public static JmmParserResult parse(String code) {
        return parse(code, Collections.emptyMap());
    }

    public static JmmParserResult parse(String code, Map<String, String> config) {

        JmmParser parser = getJmmParser();

        return parser.parse(code, config);
    }

    private static String getClassFromConfig(String property) {
        // Get class name
        String className = CONFIG.getProperty(property);

        // Check if empty
        if (className.isBlank()) {
            throw new RuntimeException("Possible problem in file 'config.properties', property '" + property
                    + "' is empty. Please provide a fully qualified class name for that compilation stage.");
        }

        return className;
    }

    public static JmmParser getJmmParser() {

        SpecsSystem.programStandardInit();

        // Get Parser class
        String parserClassName = getClassFromConfig("ParserClass");

        try {
            // Get class with main
            Class<?> parserClass = Class.forName(parserClassName);

            // It is expected that the Parser class can be instantiated without arguments
            return (JmmParser) parserClass.getConstructor().newInstance();

        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate JmmParser from class '" + parserClassName + "'", e);
        }
    }

    public static JmmAnalysis getJmmAnalysis() {

        SpecsSystem.programStandardInit();

        // Get Analysis class
        String analysisClassName = getClassFromConfig("AnalysisClass");

        try {

            // Get class with main
            Class<?> analysisClass = Class.forName(analysisClassName);

            // It is expected that the Analysis class can be instantiated without arguments
            JmmAnalysis analysis = (JmmAnalysis) analysisClass.getConstructor().newInstance();
            return analysis;
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate JmmAnalysis from class '" + analysisClassName + "'", e);
        }
    }

    public static JmmOptimization getJmmOptimization() {

        SpecsSystem.programStandardInit();

        // Get Optimization class
        String optimizeClassName = getClassFromConfig("OptimizationClass");

        try {

            // Get class with main
            Class<?> optimizeClass = Class.forName(optimizeClassName);

            // It is expected that the Optimize class can be instantiated without arguments
            JmmOptimization optimization = (JmmOptimization) optimizeClass.getConstructor().newInstance();
            return optimization;

        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate JmmOptimization from class '" + optimizeClassName + "'",
                    e);
        }
    }

    public static JasminBackend getJasminBackend() {

        SpecsSystem.programStandardInit();

        // Get Backend class
        String backendClassName = getClassFromConfig("BackendClass");

        try {
            // Get class with main
            Class<?> backendClass = Class.forName(backendClassName);

            // It is expected that the Backend class can be instantiated without arguments
            JasminBackend backend = (JasminBackend) backendClass.getConstructor().newInstance();
            return backend;

        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate JasminBackend from class '" + backendClassName + "'",
                    e);
        }
    }

    public static boolean hasAstToJasminClass() {
        return CONFIG.getProperty("AstToJasminClass") != null;
    }

    public static AstToJasmin getAstToJasmin() {

        SpecsSystem.programStandardInit();

        // Get Optimization class
        String astToJasminClassName = getClassFromConfig("AstToJasminClass");

        try {

            // Get class with main
            Class<?> astToJasminClass = Class.forName(astToJasminClassName);

            // It is expected that the AstToJasmin class can be instantiated without arguments
            AstToJasmin astToJasmin = (AstToJasmin) astToJasminClass.getConstructor().newInstance();
            return astToJasmin;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not instantiate AstToJasmin from class '" + astToJasminClassName + "'",
                    e);
        }
    }

    /**
     * Only calls the `JmmAnalysis` stage to analyse the AST.
     * 
     * @param parserResult
     * @return
     */
    public static JmmSemanticsResult analyse(JmmParserResult parserResult) {

        JmmAnalysis analysis = getJmmAnalysis();

        return analysis.semanticAnalysis(parserResult);
    }

    /**
     * Receives a string o Java-- code and calls JmmParser and JmmAnalysis stages to generate and analyse the AST.
     * Assumes there is no configuration.
     * 
     * @param jmmCode
     * @return
     */
    public static JmmSemanticsResult analyse(String jmmCode) {
        return analyse(jmmCode, Collections.emptyMap());
    }

    /**
     * Receives a string o Java-- code and calls JmmParser and JmmAnalysis stages to generate and analyse the AST.
     * 
     * @param jmmCode
     * @param config
     * @return
     */
    public static JmmSemanticsResult analyse(String jmmCode, Map<String, String> config) {
        var parseResults = TestUtils.parse(jmmCode, config);
        noErrors(parseResults.getReports());
        return analyse(parseResults);
    }

    /**
     * Only calls the `JmmOptimization` stage to optimize and generate the OLLIR code.
     * 
     * @param semanticsResult
     * @return
     */
    public static OllirResult optimize(JmmSemanticsResult semanticsResult) {

        JmmOptimization optimization = getJmmOptimization();

        semanticsResult = optimization.optimize(semanticsResult);

        var ollirResult = optimization.toOllir(semanticsResult);

        ollirResult = optimization.optimize(ollirResult);

        return ollirResult;

    }

    /**
     * Receives a string o Java-- code and calls JmmParser and JmmAnalysis stages to generate and analyse the AST, and
     * JmmOptimization to optimize and generate OLLIR code.
     * 
     * @param jmmCode
     * @param config
     * @return
     */
    public static OllirResult optimize(String jmmCode, Map<String, String> config) {
        var semanticsResult = analyse(jmmCode, config);
        noErrors(semanticsResult.getReports());
        return optimize(semanticsResult);
    }

    /**
     * Receives a string o Java-- code and calls JmmParser and JmmAnalysis stages to generate and analyse the AST, and
     * JmmOptimization to optimize and generate OLLIR code. Assumes there is no configuration.
     * 
     * @param jmmCode
     * @return
     */
    public static OllirResult optimize(String jmmCode) {
        return optimize(jmmCode, Collections.emptyMap());
    }

    /**
     * Only calls the `JasminBackend` stage to generate Jasmin code.
     * 
     * @param ollirResult
     * @return
     */
    public static JasminResult backend(OllirResult ollirResult) {
        JasminBackend backend = getJasminBackend();

        var jasminResult = backend.toJasmin(ollirResult);

        return jasminResult;

    }

    /**
     * Only calls the `JasminBackend` stage to generate Jasmin code.
     * 
     * @param ollirResult
     * @return
     */
    public static JasminResult backend(JmmSemanticsResult semanticsResult) {
        var astToJasmin = getAstToJasmin();

        // Optimize
        semanticsResult = astToJasmin.optimize(semanticsResult);

        // Convert
        var jasminResult = astToJasmin.toJasmin(semanticsResult);

        return jasminResult;
    }

    /**
     * Receives a string o Java-- code and calls all the stages to generate Jasmin code. Assumes there is no
     * configuration.
     * 
     * @param jmmCode
     * @return
     */
    public static JasminResult backend(String jmmCode) {
        return backend(jmmCode, Collections.emptyMap());
    }

    /**
     * Receives a string o Java-- code and calls all the stages to generate Jasmin code.
     * 
     * @param jmmCode
     * @return
     */
    public static JasminResult backend(String code, Map<String, String> config) {
        // AstToJasmin path has priority
        if (hasAstToJasminClass()) {
            var semanticsResult = analyse(code, config);
            noErrors(semanticsResult.getReports());
            return backend(semanticsResult);
        }

        // Otherwise, run OLLIR path
        var ollirResult = optimize(code, config);
        noErrors(ollirResult.getReports());
        return backend(ollirResult);
    }

    /**
     * Checks if there are no Error reports. Throws exception if there is at least one Report of type Error.
     */
    public static void noErrors(List<Report> reports) {
        reports.stream()
                .filter(report -> report.getType() == ReportType.ERROR)
                .findFirst()
                .ifPresent(report -> {
                    if (report.getException().isPresent()) {
                        throw new RuntimeException("Found at least one error report: " + report,
                                report.getException().get());
                    }

                    throw new RuntimeException("Found at least one error report: " + report);
                });
    }

    /**
     * Overload that accepts a ReportsProvider.
     * 
     * @param provider
     */
    public static void noErrors(ReportsProvider provider) {
        noErrors(provider.getReports());
    }

    /**
     * Checks if there are Error reports. Throws exception is there are no reports of type Error.
     */
    public static void mustFail(List<Report> reports) {
        boolean noReports = reports.stream()
                .filter(report -> report.getType() == ReportType.ERROR)
                .findFirst()
                .isEmpty();

        if (noReports) {
            throw new RuntimeException("Could not find any Error report");
        }
    }

    /**
     * Overload that accepts a ReportsProvider.
     * 
     * @param provider
     */
    public static void mustFail(ReportsProvider provider) {
        mustFail(provider.getReports());
    }

    public static long getNumReports(List<Report> reports, ReportType type) {
        return reports.stream()
                .filter(report -> report.getType() == type)
                .count();
    }

    public static long getNumErrors(List<Report> reports) {
        return getNumReports(reports, ReportType.ERROR);
    }

    public static String getLibsClasspath() {
        // return "test/fixtures/libs/compiled";
        return "libs-jmm/compiled";
    }

    public static String runJasmin(String jasminCode) {
        // return new JasminResult(jasminCode).run();
        return runJasmin(jasminCode, Collections.emptyMap());
    }

    public static String runJasmin(String jasminCode, List<String> args) {
        // return new JasminResult(jasminCode).run(args);
        return runJasmin(jasminCode, args, Collections.emptyMap());
    }

    public static String runJasmin(String jasminCode, Map<String, String> config) {
        return new JasminResult(jasminCode, config).run();
    }

    public static String runJasmin(String jasminCode, List<String> args, Map<String, String> config) {
        return new JasminResult(jasminCode, config).run(args);
    }

    /**
     * Converts an even number of Strings to a key->value map of Strings.
     * 
     * <p>
     * E.g. if args is ["key1", "value1", "key2", "value2"], returns a map {"key1": "value1", "key2": "value2"}
     * 
     * @param args
     * @return
     */
    public static Map<String, String> toConfig(List<String> args) {
        SpecsCheck.checkArgument(args.size() % 2 == 0,
                () -> "Expected an even number of arguments, got " + args.size() + ": " + args);

        // Using LinkedHashMap to keep order of arguments
        var config = new LinkedHashMap<String, String>();
        for (int i = 0; i < args.size(); i += 2) {
            config.put(args.get(i), args.get(i + 1));
        }

        return config;
    }

    /**
     * Convenience method that receives a variable number of arguments.
     * 
     * @param args
     * @return
     */
    public static Map<String, String> toConfig(String... args) {
        return toConfig(Arrays.asList(args));
    }

    /**
     * Finds an exception of the given class inside the chain of causes of the given exception. If the given exception
     * is an instance of the given class, returns the exception itself. Returns null if no exception of the given class
     * is found.
     * 
     * @param <T>
     * @param e
     * @param expectedClass
     * @return
     */
    public static <T extends Throwable> T getException(Throwable e, Class<T> expectedClass) {
        Throwable currentException = e;

        while (currentException != null) {
            if (expectedClass.isInstance(currentException)) {
                return expectedClass.cast(currentException);
            }

            currentException = currentException.getCause();
        }

        return null;
    }
}