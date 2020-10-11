public class TestResult {
    private final String testName;
    private final TestExecutionStatus testExecutionStatus;
    private final Exception error;

    public TestResult(String testName, TestExecutionStatus testExecutionStatus) {
        this.testName = testName;
        this.testExecutionStatus = testExecutionStatus;
        error = null;
    }

    public TestResult(String testName, TestExecutionStatus testExecutionStatus, Exception error) {
        this.testName = testName;
        this.testExecutionStatus = testExecutionStatus;
        this.error = error;
    }

    public String getTestName() { return testName; }

    public TestExecutionStatus getTestExecutionStatus() {
        return testExecutionStatus;
    }

    public Exception getError() {
        return error;
    }

    public String toFormattedString() {
        var value = new StringBuilder();

        value.append(String.format("\t%s %s", testExecutionStatus.toFormattedString(), testName));

        if (error != null) {
            value.append(String.format("%n\t\t--> %s", error.getCause().getMessage()));
        }

        return value.toString();
    }

    @Override
    public String toString() {
        var value = new StringBuilder();

        value.append(String.format("\t%s %s", testExecutionStatus, testName));

        if (error != null) {
            value.append(String.format("%n\t\t--> %s", error.getCause().getMessage()));
        }

        return value.toString();
    }
}
