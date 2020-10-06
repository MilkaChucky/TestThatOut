import annotations.TestCase;
import org.reflections8.Reflections;
import java.util.Set;
import java.util.stream.Collectors;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("TestThatOut started");

        Reflections reflections = new Reflections();
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(TestCase.class);
        System.out.println(classes.stream().map(Class::getSimpleName).collect(Collectors.joining()));
    }
}
