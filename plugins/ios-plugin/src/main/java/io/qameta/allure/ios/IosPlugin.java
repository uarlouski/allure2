package io.qameta.allure.ios;

import io.qameta.allure.Reader;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xmlwise.Plist;
import xmlwise.XmlParseException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.nio.file.Files.newDirectoryStream;
import static java.util.Collections.emptyList;

/**
 * @author charlie (Dmitry Baev).
 */
public class IosPlugin implements Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(IosPlugin.class);

    private static final String TESTABLE_SUMMARIES = "TestableSummaries";
    private static final String TESTS = "Tests";
    private static final String SUB_TESTS = "Subtests";
    private static final String TEST_STATUS = "TestStatus";
    private static final String TEST_NAME = "TestName";
    private static final String TEST_IDENTIFIER = "TestIdentifier";
    private static final String TITLE = "Title";
    private static final String SUB_ACTIVITIES = "SubActivities";
    private static final String ACTIVITY_SUMMARIES = "ActivitySummaries";

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        final List<Path> files = listResults(directory);
        for (Path file : files) {
            try {
                LOGGER.info("Parse file {}", file);
                final Map<String, Object> loaded = Plist.load(file.toFile());
                final List<?> summaries = asList(loaded.getOrDefault(TESTABLE_SUMMARIES, emptyList()));
                summaries.forEach(summary -> parseSummary(summary, visitor));
            } catch (XmlParseException | IOException e) {
                LOGGER.error("Could not parse file {}: {}", file, e);
            }
        }
    }

    private void parseSummary(final Object summary, final ResultsVisitor visitor) {
        final Map<String, Object> props = asMap(summary);
        final List<Object> tests = asList(props.getOrDefault(TESTS, emptyList()));
        tests.forEach(test -> parseTestSuite(test, visitor));
    }

    @SuppressWarnings("unchecked")
    private void parseTestSuite(final Object testSuite, final ResultsVisitor visitor) {
        Map<String, Object> props = asMap(testSuite);
        if (isTest(props)) {
            parseTest(testSuite, visitor);
            return;
        }

        final Object tests = props.get(SUB_TESTS);
        if (Objects.nonNull(tests)) {
            final List<?> subTests = List.class.cast(tests);
            subTests.forEach(subTest -> parseTestSuite(subTest, visitor));
        }
    }

    private void parseTest(final Object test, final ResultsVisitor visitor) {
        Map<String, Object> props = asMap(test);
        final List<Step> steps = asList(props.getOrDefault(ACTIVITY_SUMMARIES, emptyList())).stream()
                .map(this::parseStep)
                .collect(Collectors.toList());
        final TestResult result = new TestResult()
                .withName(getTestName(props))
                .withStatus(getTestStatus(props))
                .withFullName(getFullName(props))
                .withUid(UUID.randomUUID().toString());
        if (!steps.isEmpty()) {
            result.setTestStage(new StageResult().withSteps(steps));
        }
        visitor.visitTestResult(result);
    }

    private Step parseStep(final Object step) {
        final Map<String, Object> props = asMap(step);
        final String stepName = (String) props.get(TITLE);
        final List<Step> subSteps = asList(props.getOrDefault(SUB_ACTIVITIES, emptyList())).stream()
                .map(this::parseStep)
                .collect(Collectors.toList());
        return new Step()
                .withName(stepName)
                .withStatus(Status.PASSED)
                .withSteps(subSteps);
    }

    private String getTestName(final Map<String, Object> props) {
        return (String) props.getOrDefault(TEST_NAME, "Unknown");
    }

    private String getFullName(final Map<String, Object> props) {
        return (String) props.get(TEST_IDENTIFIER);
    }

    private Status getTestStatus(final Map<String, Object> props) {
        final Object status = props.get(TEST_STATUS);
        if (Objects.isNull(status)) {
            return Status.UNKNOWN;
        }
        if ("Success".equals(status)) {
            return Status.PASSED;
        }
        if ("Failure".equals(status)) {
            return Status.FAILED;
        }
        return Status.UNKNOWN;
    }

    @SuppressWarnings("unchecked")
    private List<Object> asList(final Object object) {
        return List.class.cast(object);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(final Object object) {
        return Map.class.cast(object);
    }

    private boolean isTest(final Map<String, Object> props) {
        return props.containsKey(TEST_STATUS);
    }

    private static List<Path> listResults(final Path directory) {
        List<Path> result = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return result;
        }

        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, "*.plist")) {
            for (Path path : directoryStream) {
                if (!Files.isDirectory(path)) {
                    result.add(path);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not read data from {}: {}", directory, e);
        }
        return result;
    }
}
