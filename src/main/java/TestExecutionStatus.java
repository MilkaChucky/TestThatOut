public enum TestExecutionStatus {
    FAILED(String.format("%s[]%s", AnsiColor.RED_BOLD_BRIGHT, AnsiColor.RESET), "✘"),
    SUCCEEDED(String.format("%s[]%s", AnsiColor.GREEN_BOLD_BRIGHT, AnsiColor.RESET), "✔"),
    SKIPPED(String.format("%s[]%s", AnsiColor.YELLOW_BOLD_BRIGHT, AnsiColor.RESET), "►"),
    ABORTED(String.format("%s[]%s", AnsiColor.MAGENTA_BOLD_BRIGHT, AnsiColor.RESET), " ");

    private final String template;
    private final String symbol;
    private Boolean useSymbol;

    public static boolean useSymbols = false;

    TestExecutionStatus(String template, String symbol) {
        this.template = template;
        this.symbol = symbol;
        useSymbol = null;
    }

    public TestExecutionStatus useSymbol(boolean value) {
        useSymbol = value;
        return this;
    }

    public String toFormattedString() {
        var withSymbol = useSymbol == null ? useSymbols : useSymbol;
        return template.replace("[]", String.format("[%s]", withSymbol ? symbol : name()));
    }

    @Override
    public String toString() {
        var withSymbol = useSymbol == null ? useSymbols : useSymbol;
        return String.format("[%s]", withSymbol ? symbol : name());
    }
}
