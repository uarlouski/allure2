package io.qameta.allure.ios;

import io.qameta.allure.entity.StageResult;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Step;
import io.qameta.allure.entity.TestResult;
import io.qameta.allure.entity.Time;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The collection of Test result utils methods.
 */
public final class ResultsUtils {

    private static final String TEST_NAME = "TestName";
    private static final String TEST_STATUS = "TestStatus";
    private static final String TEST_DURATION = "Duration";
    private static final String TEST_IDENTIFIER = "TestIdentifier";

    private static final String STEP_NAME = "Title";
    private static final String STEP_START_TIME = "StartTimeInterval";
    private static final String STEP_STOP_TIME = "FinishTimeInterval";

    private ResultsUtils() {
    }

    public static TestResult getTestResult(final Map<String, Object> props) {
        return new TestResult()
                .withUid(UUID.randomUUID().toString())
                .withName(ResultsUtils.getTestName(props))
                .withStatus(ResultsUtils.getTestStatus(props))
                .withFullName(ResultsUtils.getFullName(props))
                .withTime(ResultsUtils.getTestTime(props))
                .withTestStage(new StageResult());
    }

    public static boolean isTest(final Map<String, Object> props) {
        return props.containsKey(TEST_STATUS);
    }

    public static String getTestName(final Map<String, Object> props) {
        return (String) props.getOrDefault(TEST_NAME, "Unknown");
    }

    private static String getFullName(final Map<String, Object> props) {
        return (String) props.getOrDefault(TEST_IDENTIFIER, "Unknown");
    }

    private static Time getTestTime(final Map<String, Object> props) {
        return new Time().withDuration(parseTime(props.getOrDefault(TEST_DURATION, "0").toString()));
    }

    private static Status getTestStatus(final Map<String, Object> props) {
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


    public static Step getStep(final Map<String, Object> props) {
        return new Step()
                .withName(getStepName(props))
                .withTime(getStepTime(props))
                .withStatus(Status.PASSED);
    }

    private static String getStepName(final Map<String, Object> props) {
        return (String) props.getOrDefault(STEP_NAME, "Unknown");
    }

    private static Time getStepTime(final Map<String, Object> props) {
        long start = parseTime(props.getOrDefault(STEP_START_TIME, "0").toString());
        long stop = parseTime(props.getOrDefault(STEP_STOP_TIME, "0").toString());
        return new Time().withStart(start).withStop(stop).withDuration(stop - start);
    }

    private static long parseTime(final String time) {
        final Double doubleTime = Double.parseDouble(time);
        final int seconds = doubleTime.intValue();
        return seconds * 1000;
    }
}
