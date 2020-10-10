public class TestResult {
    private final TestExecutionStatus testExecutionStatus;
    private final Exception error;

    public TestResult(TestExecutionStatus testExecutionStatus) {
        this.testExecutionStatus = testExecutionStatus;
        error = null;
    }

    public TestResult(TestExecutionStatus testExecutionStatus, Exception error) {
        this.testExecutionStatus = testExecutionStatus;
        this.error = error;
    }

    public TestExecutionStatus getTestExecutionStatus() {
        return testExecutionStatus;
    }

    public Exception getError() {
        return error;
    }
}
