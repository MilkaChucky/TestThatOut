package testcases;

import annotations.*;

@TestCase
public class TestCase1 {

    @BeforeClass
    public void prologue() {}

    @Before
    public void beforeTests() {}

    @Test
    public void test1() {}

    @Test
    @Skip
    public void skippedTest() {}

    @After
    public void afterTests() {}

    @AfterClass
    public void epilogue() {}
}
