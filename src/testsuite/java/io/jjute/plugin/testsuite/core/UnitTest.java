package io.jjute.plugin.testsuite.core;

import org.junit.jupiter.api.TestInstance;

/**
 * Unit tests aims to verify the smallest unit of code. In Java-based projects this unit
 * is a method. Unit tests usually do not interact with other parts of the system e.g. a
 * database or the file system. Interactions with other parts of the system are usually
 * cut off with the help of Stubs or Mocks. You will find that POJOs and utility classes are
 * good candidates for unit tests as they are self-contained and do not use the Gradle API.
 *
 * @see <a href="https://guides.gradle.org/testing-gradle-plugins/#testing-pyramid">
 *      Gradle Docs: The testing pyramid</a>
 */
//@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class UnitTest extends PluginTest {

    protected UnitTest() {
        super(Type.UNIT);
    }
}
