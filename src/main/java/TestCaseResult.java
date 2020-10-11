import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestCaseResult {
    private final String testCaseName;
    private final Map<String, TestResult> testResults;

    public TestCaseResult(String name, Map<String, TestResult> testResults) {
        this.testCaseName = name;
        this.testResults = testResults;
    }

    public String getTestCaseName() { return testCaseName; }

    public TestResult getTestResult(String testName) { return testResults.get(testName); }

    public List<TestResult> getTestResults() {
        return new ArrayList<>(testResults.values());
    }

    public List<TestResult> getTestResults(TestExecutionStatus status) {
        return testResults.values().stream()
                .filter(testResult -> testResult.getTestExecutionStatus() == status)
                .collect(Collectors.toList());
    }

    public String toFormattedString() {
        var value = new StringBuilder();
        value.append(String.format("%s%s%s%s", AnsiColor.WHITE_UNDERLINED, AnsiColor.WHITE_BOLD_BRIGHT, testCaseName, AnsiColor.RESET));

        for (var testResult : testResults.values()) {
            value.append(String.format("%n%s", testResult.toFormattedString()));
        }

        return value.toString();
    }

    @Override
    public String toString() {
        var value = new StringBuilder();
        value.append(testCaseName);

        for (var testResult : testResults.values()) {
            value.append(String.format("%n%s", testResult));
        }

        return value.toString();
    }
}
