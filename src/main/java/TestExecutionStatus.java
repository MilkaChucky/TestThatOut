public enum TestExecutionStatus {
    FAILED(String.format("%s[]%s", AnsiColor.RED_BOLD_BRIGHT, AnsiColor.RESET), "✘"),
    SUCCEEDED(String.format("%s[]%s", AnsiColor.GREEN_BOLD_BRIGHT, AnsiColor.RESET), "✔"),
    SKIPPED(String.format("%s[]%s", AnsiColor.YELLOW_BOLD_BRIGHT, AnsiColor.RESET), "►"),
    ABORTED(String.format("%s[]%s", AnsiColor.MAGENTA_BOLD_BRIGHT, AnsiColor.RESET), "■");

    private final String template;
    private final String symbol;
    private boolean useSymbol;

    TestExecutionStatus(String template, String symbol) {
        this.template = template;
        this.symbol = symbol;
        useSymbol = false;
    }

    public TestExecutionStatus useSymbol(boolean value) {
        useSymbol = value;
        return this;
    }

    @Override
    public String toString() {
        return template.replace("[]", String.format("[%s]", useSymbol ? symbol : name()));
    }
}
