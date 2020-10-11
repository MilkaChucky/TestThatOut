package testcases;

import annotations.*;

@TestCase
public class TestCase3 {
    private int i;

    @BeforeClass
    public void prologue() {
        i = 0;
    }

    @Before
    public void beforeTests() throws Exception {
        if (++i == 2) {
            throw new Exception("Second test fails on 'Before' method.");
        }
    }

    @Test
    public void test1() {}

    @Test
    public void test2() {}

    @Test
    public void test3() {}

    @After
    public void afterTests() {
        System.out.printf("i = %d%n", i);
    }
}
