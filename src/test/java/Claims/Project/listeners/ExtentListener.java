package Claims.Project.listeners;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.openqa.selenium.*;
import org.testng.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExtentListener implements ITestListener {

    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    // ---------------- START SUITE ----------------
    @Override
    public void onStart(ITestContext context) {

        String timeStamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String reportPath = System.getProperty("user.dir")
                + "/reports/ExtentReport_" + timeStamp + ".html";

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setReportName("Claims Concierge Automation Report");
        spark.config().setDocumentTitle("Dashboard Automation");

        extent = new ExtentReports();
        extent.attachReporter(spark);

        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("Browser", "Chrome");
        extent.setSystemInfo("Environment", "QA");
    }

    // ---------------- TEST START ----------------
    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest =
                extent.createTest(result.getMethod().getMethodName());
        test.set(extentTest);
    }

    // ---------------- TEST PASS ----------------
    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().pass("✅ Test Passed Successfully");
    }

    // ---------------- TEST FAIL ----------------
    @Override
    public void onTestFailure(ITestResult result) {

        ExtentTest extentTest = test.get();
        extentTest.fail("❌ Test Failed");
        extentTest.fail(result.getThrowable());

        try {
            Object testClass = result.getInstance();
            Field driverField = testClass.getClass().getDeclaredField("driver");
            driverField.setAccessible(true);
            WebDriver driver = (WebDriver) driverField.get(testClass);

            if (driver != null) {

                File src = ((TakesScreenshot) driver)
                        .getScreenshotAs(OutputType.FILE);

                String timeStamp = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

                String screenshotDir =
                        System.getProperty("user.dir") + "/reports/screenshots/";

                new File(screenshotDir).mkdirs();

                String screenshotPath = screenshotDir
                        + result.getMethod().getMethodName()
                        + "_" + timeStamp + ".png";

                Files.copy(src.toPath(),
                        new File(screenshotPath).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);

                extentTest.addScreenCaptureFromPath(screenshotPath);

            } else {
                extentTest.warning("⚠ WebDriver instance was null. Screenshot not captured.");
            }

        } catch (NoSuchFieldException | IllegalAccessException | IOException e) {
            extentTest.warning("⚠ Screenshot capture failed: " + e.getMessage());
        }
    }

    // ---------------- TEST SKIP ----------------
    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().skip("⏭ Test Skipped");
        if (result.getThrowable() != null) {
            test.get().skip(result.getThrowable());
        }
    }

    // ---------------- FINISH SUITE ----------------
    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }
}
