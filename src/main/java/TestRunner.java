import annotations.*;
import org.jetbrains.annotations.NotNull;
import org.reflections8.Reflections;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestRunner {
    public static void main(String[] args) {
        Reflections reflections = new Reflections();
        Stream<Class<?>> classes = reflections.getTypesAnnotatedWith(TestCase.class).stream();

        if (args != null && args.length > 0) {
            classes = classes.filter(c -> Arrays.stream(args).anyMatch(name -> name.equals(c.getSimpleName())));
        }

        var testResults = classes.parallel()
                .collect(Collectors.toMap(Class::getSimpleName, testCase -> {
                    try {
                        return RunTestCase(testCase);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(ex.hashCode());
                    }
                    return null;
                }));

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
        var results = new LinkedHashMap<String, TestResult>();

        var beforeClassTests = new LinkedList<Method>();
        var beforeTests = new LinkedList<Method>();
        var tests = new LinkedList<Method>();
        var afterTests = new LinkedList<Method>();
        var afterClassTests = new LinkedList<Method>();

//        results.put(String.format("%s init", testCase.getSimpleName()), null);

        for (var method : testCase.getDeclaredMethods()) {
            if (method.getAnnotation(BeforeClass.class) != null) {
                beforeClassTests.add(method);
//                results.put(String.format("BeforeClass: %s", method.getName()), null);
            }

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

            if (method.getAnnotation(AfterClass.class) != null) {
                afterClassTests.add(method);
//                results.put(String.format("AfterClass: %s", method.getName()), null);
            }
        }

        Object testCaseInstance;
        try {
            testCaseInstance = testCase.getConstructor().newInstance();
        } catch (Exception ex) {
//            results.put(String.format("%s init", testCase.getSimpleName()), new TestResult(TestExecutionStatus.FAILED, ex));

            return results;
        }

        for (var beforeClassTest : beforeClassTests) {
//            var name = String.format("BeforeClass: %s", beforeClassTest.getName());
            try {
                beforeClassTest.invoke(testCaseInstance);
//                results.remove(name);
            } catch (Exception ex) {
//                results.put(name, new TestResult(TestExecutionStatus.FAILED, ex));
//                return results;
            }
        }

        for (var test : tests) {
            try {
                for (var beforeTest : beforeTests) {
                    beforeTest.invoke(testCaseInstance);
                }

                test.invoke(testCaseInstance);
                results.put(test.getName(), new TestResult(TestExecutionStatus.SUCCEEDED));

                for (var afterTest : afterTests) {
                    afterTest.invoke(testCaseInstance);
                }
            } catch (Exception ex) {
                results.put(test.getName(), new TestResult(TestExecutionStatus.FAILED, ex));
            }
        }

        for (var afterClassTest : afterClassTests) {
//            var name = String.format("AfterClass: %s", afterClassTest.getName());
            try {
                afterClassTest.invoke(testCaseInstance);
//                results.remove(name);
            } catch (Exception ex) {
//                results.put(name, new TestResult(TestExecutionStatus.FAILED, ex));
            }
        }

        return results;
    }
}
