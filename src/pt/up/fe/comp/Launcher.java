package pt.up.fe.comp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Map;

import pt.up.fe.comp.backend.JmmBackend;
import pt.up.fe.comp.cli.CLIArgumentsParser;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.optimization.JmmOptimizer;
import pt.up.fe.comp.parser.SimpleParser;
import pt.up.fe.comp.semantic.JmmAnalyser;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) throws IOException {
        SpecsSystem.programStandardInit();
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        Map<String, String> config = (new CLIArgumentsParser(args)).getConfig();

        String fileName = config.get("inputFile");
        File inputFile = new File(fileName);
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + fileName + "'.");
        }
        String input = SpecsIo.read(inputFile);

        // Create config

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(input, config);

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());

        // Instantiate JmmAnalysis
        JmmAnalyser analyser = new JmmAnalyser();

        // Analysis stage
        JmmSemanticsResult analysisResult = analyser.semanticAnalysis(parserResult);

        // Check if there are semantic errors
        TestUtils.noErrors(analysisResult.getReports());

        // Instantiate JmmOptimizer
        JmmOptimizer optimizer = new JmmOptimizer();

        // Optimize at AST level
        analysisResult = optimizer.optimize(analysisResult);

        // Generate ollir code
        OllirResult ollirResult = optimizer.toOllir(analysisResult);

        // Optimize at ollir level
        ollirResult = optimizer.optimize(ollirResult);

        // Check if there are no ollir errors
        TestUtils.noErrors(ollirResult.getReports());

        // Save ollir file
        try (FileWriter writer = new FileWriter(config.get("className") + ".ollir")) {
            writer.write(ollirResult.getOllirCode());
        }

        // Instantiate JmmBackend
        JmmBackend backend = new JmmBackend();

        // Generate jasmin code
        JasminResult jasminResult = backend.toJasmin(ollirResult);

        // Check if there are no jasmin errors
        TestUtils.noErrors(jasminResult.getReports());

        // Save jasmin file
        try (FileWriter writer = new FileWriter(config.get("className") + ".j")) {
            writer.write(jasminResult.getJasminCode());
        }

        // Save compiled file
        File compilationResult = jasminResult.compile();
        Files.copy(compilationResult.toPath(), new File(config.get("className") + ".class").toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
