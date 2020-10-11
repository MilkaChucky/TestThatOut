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
        TestExecutionStatus.useSymbols = true;

        var testCaseResults = runAnnotatedTestCases(args);

//        var testResults = runTestCases(
//                TestCase1.class,
//                TestCase3.class
//        );

        for (var testCaseResult : testCaseResults) {
            String test = testCaseResult.toString();
            System.out.println(testCaseResult.toFormattedString());
            System.out.println();
        }

//        var failedTests = testCaseResults.stream()
//                .flatMap(testCaseResult -> testCaseResult.getTestResults(TestExecutionStatus.FAILED).stream())
//                .collect(Collectors.toList());
//
//        for (var testResult : failedTests) {
//            System.out.println(testResult);
//        }
    }

    public static List<TestCaseResult> runAnnotatedTestCases(String... testCaseNames) {
        Stream<Class<?>> testCases = reflections.getTypesAnnotatedWith(TestCase.class).stream();

        if (testCaseNames != null && testCaseNames.length > 0) {
            testCases = testCases.filter(c -> Arrays.stream(testCaseNames).anyMatch(name -> name.equals(c.getSimpleName())));
        }

        return runTestCases(testCases);
    }

    public static List<TestCaseResult> runTestCases(@NotNull Class<?>... testCases) {
        return runTestCases(Arrays.stream(testCases));
    }

    public static List<TestCaseResult> runTestCases(@NotNull Stream<Class<?>> testCases) {
        return testCases.parallel()
                .map(TestRunner::runTestCase)
                .collect(Collectors.toList());
    }

    private static TestCaseResult runTestCase(@NotNull Class<?> testCase) {
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
                results.put(method.getName(), new TestResult(method.getName(), TestExecutionStatus.ABORTED));
            } else if (method.getAnnotation(Skip.class) != null) {
                results.put(method.getName(), new TestResult(method.getName(), TestExecutionStatus.SKIPPED));
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
            results.put(0, name, new TestResult(name, TestExecutionStatus.FAILED, ex));

            return new TestCaseResult(testCase.getSimpleName(), results);
        }

        for (var beforeClassTest : beforeClassTests) {
            try {
                beforeClassTest.invoke(testCaseInstance);
            } catch (Exception ex) {
                results.put(0, beforeClassTest.getName(), new TestResult(beforeClassTest.getName(), TestExecutionStatus.FAILED, ex));

                return new TestCaseResult(testCase.getSimpleName(), results);
            }
        }

        for (var test : tests) {
            for (var beforeTest : beforeTests) {
                try {
                    beforeTest.invoke(testCaseInstance);
                } catch (Exception ex) {
                    var name = String.format("Before %s: %s", test.getName(), beforeTest.getName());
                    var i = results.indexOf(test.getName());
                    results.put(i, name, new TestResult(name, TestExecutionStatus.FAILED, ex));

                    return new TestCaseResult(testCase.getSimpleName(), results);
                }
            }

            try {
                test.invoke(testCaseInstance);
                results.put(test.getName(), new TestResult(test.getName(), TestExecutionStatus.SUCCEEDED));
            } catch (Exception ex) {
                results.put(test.getName(), new TestResult(test.getName(), TestExecutionStatus.FAILED, ex));
            }

            for (var afterTest : afterTests) {
                try {
                    afterTest.invoke(testCaseInstance);
                } catch (Exception ex) {
                    var name = String.format("After %s: %s", test.getName(), afterTest.getName());
                    var i = results.indexOf(test.getName()) + 1;
                    results.put(i, name, new TestResult(name, TestExecutionStatus.FAILED, ex));

                    return new TestCaseResult(testCase.getSimpleName(), results);
                }
            }
        }

        for (var afterClassTest : afterClassTests) {
            try {
                afterClassTest.invoke(testCaseInstance);
            } catch (Exception ex) {
                results.put(afterClassTest.getName(), new TestResult(afterClassTest.getName(), TestExecutionStatus.FAILED, ex));
            }
        }

        return new TestCaseResult(testCase.getSimpleName(), results);
    }
}
