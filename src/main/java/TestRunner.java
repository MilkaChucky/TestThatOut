import annotations.*;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.NotNull;
import org.reflections8.Reflections;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestRunner {
    private static final Reflections reflections = new Reflections();

    public static void main(String[] args) {
        Stream<Class<?>> classes = reflections.getTypesAnnotatedWith(TestCase.class).stream();

        if (args != null && args.length > 0) {
            classes = classes.filter(c -> Arrays.stream(args).anyMatch(name -> name.equals(c.getSimpleName())));
        }

        var testResults = classes.parallel()
                .collect(Collectors.toMap(Class::getSimpleName, TestRunner::RunTestCase));

        for (var testCase : testResults.entrySet()) {
            System.out.printf("%s%s%s%s%n", AnsiColor.WHITE_UNDERLINED, AnsiColor.WHITE_BOLD_BRIGHT, testCase.getKey(), AnsiColor.RESET);

            for (var test : testCase.getValue().entrySet()) {
                System.out.printf("\t%s %s%n", test.getValue().getTestExecutionStatus().useSymbol(true), test.getKey());

                var error = test.getValue().getError();
                if (error != null) {
                    System.out.printf("\t\t--> %s%n", error.getCause().getMessage());
                }
            }

            System.out.println();
        }
    }

    private static Map<String, TestResult> RunTestCase(@NotNull Class<?> testCase) {
        var results = new ListOrderedMap<String, TestResult>();

        List<Method> // One for each annotation
                beforeClassTests = new LinkedList<>(),
                beforeTests = new LinkedList<>(),
                tests = new LinkedList<>(),
                afterTests = new LinkedList<>(),
                afterClassTests = new LinkedList<>();

        for (var method : testCase.getDeclaredMethods()) {
            if (method.getAnnotation(BeforeClass.class) != null)
                beforeClassTests.add(method);

            if (method.getAnnotation(Before.class) != null)
                beforeTests.add(method);

            if (method.getAnnotation(Test.class) != null && method.getAnnotation(Skip.class) == null) {
                tests.add(method);
                results.put(method.getName(), new TestResult(TestExecutionStatus.ABORTED));
            } else if (method.getAnnotation(Skip.class) != null) {
                results.put(method.getName(), new TestResult(TestExecutionStatus.SKIPPED));
            }

            if (method.getAnnotation(After.class) != null)
                afterTests.add(method);

            if (method.getAnnotation(AfterClass.class) != null)
                afterClassTests.add(method);
        }

        Object testCaseInstance;
        try {
            testCaseInstance = testCase.getConstructor().newInstance();
        } catch (Exception ex) {
            var name= String.format("%s init", testCase.getSimpleName());
            results.put(0, name, new TestResult(TestExecutionStatus.FAILED, ex));

            return results;
        }

        for (var beforeClassTest : beforeClassTests) {
            try {
                beforeClassTest.invoke(testCaseInstance);
            } catch (Exception ex) {
                results.put(0, beforeClassTest.getName(), new TestResult(TestExecutionStatus.FAILED, ex));

                return results;
            }
        }

        for (var test : tests) {
            for (var beforeTest : beforeTests) {
                try {
                    beforeTest.invoke(testCaseInstance);
                } catch (Exception ex) {
                    var name = String.format("Before %s: %s", test.getName(), beforeTest.getName());
                    var i = results.indexOf(test.getName());
                    results.put(i, name, new TestResult(TestExecutionStatus.FAILED, ex));

                    return results;
                }
            }

            try {
                test.invoke(testCaseInstance);
                results.put(test.getName(), new TestResult(TestExecutionStatus.SUCCEEDED));
            } catch (Exception ex) {
                results.put(test.getName(), new TestResult(TestExecutionStatus.FAILED, ex));
            }

            for (var afterTest : afterTests) {
                try {
                    afterTest.invoke(testCaseInstance);
                } catch (Exception ex) {
                    var name = String.format("After %s: %s", test.getName(), afterTest.getName());
                    var i = results.indexOf(test.getName()) + 1;
                    results.put(i, name, new TestResult(TestExecutionStatus.FAILED, ex));

                    return results;
                }
            }
        }

        for (var afterClassTest : afterClassTests) {
            try {
                afterClassTest.invoke(testCaseInstance);
            } catch (Exception ex) {
                results.put(afterClassTest.getName(), new TestResult(TestExecutionStatus.FAILED, ex));
            }
        }

        return results;
    }
}
