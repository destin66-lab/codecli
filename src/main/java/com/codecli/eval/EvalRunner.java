package com.codecli.eval;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Small batch runner that keeps execution separate from scoring. */
public final class EvalRunner {
    private final Evaluator evaluator;
    public EvalRunner() { this(new DeterministicEvaluator()); }
    public EvalRunner(Evaluator evaluator) { this.evaluator = evaluator; }

    public List<EvalReport> run(List<EvalCase> cases, Function<EvalCase, EvalObservation> executor) {
        List<EvalReport> reports = new ArrayList<>();
        for (EvalCase c : cases) {
            try { reports.add(evaluator.evaluate(c, executor.apply(c))); }
            catch (RuntimeException e) {
                reports.add(evaluator.evaluate(c, new EvalObservation(null, "", List.of(), List.of(),
                        0, false, false, true, 0, 0, 0, 0)));
            }
        }
        return reports;
    }

    /** Writes a stable machine-readable report for CI artifacts and later score import. */
    public static void writeReport(Path output, List<EvalReport> reports) throws IOException {
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.toFile(), reports);
    }

    public static List<EvalCase> loadCases(InputStream input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(input);
        List<EvalCase> result = new ArrayList<>();
        for (JsonNode n : root) {
            List<String> tools = new ArrayList<>();
            n.path("expectedTools").forEach(x -> tools.add(x.asText()));
            List<String> paths = new ArrayList<>();
            if (n.has("expectedPaths")) n.path("expectedPaths").forEach(x -> paths.add(x.asText()));
            else if (n.hasNonNull("expectedPath")) paths.add(n.path("expectedPath").asText());
            String inputText = n.hasNonNull("input") ? n.path("input").asText() : n.path("question").asText("");
            result.add(new EvalCase(n.path("id").asText("case"), inputText, tools, paths,
                    n.path("expectedText").asText(""), n.path("maxIterations").asInt(Integer.MAX_VALUE)));
        }
        return result;
    }
}
