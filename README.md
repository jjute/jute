# JJute Integration Framework

---

Jute is a *Gradle-based* integration framework for all JJute modules. It provides effective build automation, easier maintenance, dependency management and advanced features such as code coverage. All JJute modules are able to act as standalone Git submodules but benefit greatly from being developed under this framework. It was primarily designed to work with Java libraries, however it works with standard Java projects as well.

## Motivation

Working with Gradle is very rewarding but not always straightforward and easy as we would like it to be. Getting the most benefit out of this system requires plenty of conditional configurations. These configurations are then best shared between related projects that are not necessarily in the same directory hierarchy. 

Since Jute modules are designed as Git submodules and reside in separate remote repositories so they can be developed as standalone projects I needed a way to manage their interdependencies and share common configurations. This is what this framework was designed to do. It sets up the required development environment and integrates all designated modules into a single project with shared configurations on demand.

## Features

- IntelliJ IDEA integration. 
- Gradle compatible JUnit testing suite.
- Standardized source set directory layout.
- Standardized production and test output directories.
- Automatic project compatibility configuration for Java 1.8.
- Reusable library group definitions.
- Local and remote code coverage support.
- Useful utility method definitions.

## Installation

Before we can start working on JJute projects with Jute Integration Framework we need to prepare our development environment. There are two easy ways to quickly setup the framework:

- Each JJute project is designated as a Git submodule so they can all be cloned alongside Jute with the following command:  `git clone --recurse-submodules https://github.com/jjute/jute`

- Clone the framework repository and run `initModules` Gradle task. To quickly re-clone a JJute project simply delete the module directory and run the task again. It will detect the missing module and clone it again. 

## Code Coverage

Jute offers fully functional code coverage for both local and remote testing. If you are not familiar with the concept of code coverage I recommend reading about it before working with coverage reports. This [Wikipedia](https://en.wikipedia.org/wiki/Code_coverage) page is a good place to start:

> Code coverage is a measure used to describe the degree to which the source code of a program is executed when a particular test suite runs. A program with high test. A program with high test coverage, measured as a percentage, has had more of its source code executed during testing, which suggests it has a lower chance of containing undetected software bugs compared to a program with low test coverage

Local coverage reports are automatically generated using [JaCoCo](https://www.jacoco.org/jacoco/) after each test task execution. Coverage is disabled by default due to Gradle system limitations. It can only be enabled for each individual task and stays enabled within that scope only. To generate a local coverage report you have to run a test task *(or another task depending on tests)* with a dedicated initialization script that defines the necessary dependencies and configurations:

- On Windows - `gradlew test --init-script coverage.gradle`
- On Unix - `./gradlew test --init-script coverage.gradle`

*Note that this verbose `--init-script` option can be substituted with `-I` for easier use.*

Setting up a remote code coverage web service for projects can be quite difficult at times. Jute handles the heavy lifting with a [special plugin](https://github.com/kt3k/coveralls-gradle-plugin) and enables us to send reports to a web service that can then be displayed with SVG badges to publicly display the project's coverage ratio. Jute is currently using [Coveralls](https://coveralls.io/) as it's preferred choice of code coverage web service. I've tried [CodeCov](https://codecov.io) but unfortunately could never get it to work.

Remote code coverage is mostly generated and sent from a CI service. CI stands for [continual integration](https://en.wikipedia.org/wiki/Continuous_integration) which is the  practice of merging all developers' working copies to a shared mainline (project base branch) several times a day. Jute does not relay on specific CI providers but Coveralls does. Read this [page](https://docs.coveralls.io/supported-ci-services) to see a list of supported CI services. If the service you are using is not supported or you are using personal CI server you need to define a few custom environment variables to provide Coveralls support. Here is an example of how JJute Commons provides Coveralls support to JitCI: 

```bash
CI_NAME=jitCI
CI_BUILD_NUMBER=$BUILD_NR
CI_BUILD_URL=https://jitci.com/gh/jjute/commons/builds/
CI_BRANCH=$GIT_BRANCH
COVERALLS_REPO_TOKEN=abCd1efGH23ijklMn4Opr5s
```

Generating code coverage with CI also requires additional steps when we want to generate and publish coverage data for individual JJute projects. Tell the service to execute the following bash commands in specific build phases:

- **Initialization phase**: 

  <pre>
      wget "https://raw.githubusercontent.com/jjute/jute/master/coverage.gradle"
  	mkdir -p ./.gradle && mv coverage.gradle ./.gradle
  </pre>

- **Test phase**:

  *Substitute the default commands:*

  <pre>
      ./gradlew check --init-script ./.gradle/coverage.gradle
  </pre>

- **Publish phase**: 

  <pre>
      ./gradlew coveralls --init-script ./.gradle/coverage.gradle
  </pre>



