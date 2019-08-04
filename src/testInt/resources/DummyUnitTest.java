package io.jjute.test;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DummyUnitTest {

    @BeforeAll
    static void createDummyDirectory() {
        Assertions.assertTrue(new File("build/dummy/").mkdirs());
    }

    @Test @Order(1)
    void runFirstTest() throws IOException {

        System.out.println("Running first test.");
        Assertions.assertTrue(new File("build/dummy/.firstTest").createNewFile());
    }

    @Test @Order(2)
    void runSecondTest() throws IOException {

        System.out.println("Running second test.");
        Assertions.assertTrue(new File("build/dummy/.secondTest").createNewFile());
    }

    @Test @Order(3)
    void runThirdTest() throws IOException {

        System.out.println("Running third test.");
        Assertions.assertTrue(new File("build/dummy/.thirdTest").createNewFile());
    }
}
