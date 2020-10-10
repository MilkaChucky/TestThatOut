package testcases;

import annotations.*;

@TestCase
public class TestCase2 {
    @Before
    public void beforeTests() {}

    @Test
    public void test1() {}

    @Test
    public void failedTest() throws Exception {
        throw new Exception("Just testing a failed test.");
    }

    @Test
    public void test2() {}

    @AfterClass
    public void epilogue() {}
}
