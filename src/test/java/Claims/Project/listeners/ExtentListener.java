package Claims.Project.listeners;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.openqa.selenium.*;
import org.testng.*;

import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExtentListener implements ITestListener {

    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {

        String time = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        ExtentSparkReporter spark =
                new ExtentSparkReporter("reports/ExtentReport_" + time + ".html");

        spark.config().setReportName("Claims Concierge Automation Report");
        spark.config().setDocumentTitle("Dashboard Automation");

        extent = new ExtentReports();
        extent.attachReporter(spark);

        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java", System.getProperty("java.version"));
        extent.setSystemInfo("Browser", "Chrome");
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest =
                extent.createTest(result.getMethod().getMethodName());
        test.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().pass("✅ Test Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {

        ExtentTest extentTest = test.get();
        extentTest.fail(result.getThrowable());

        try {
            Object testClass = result.getInstance();
            Field driverField = testClass.getClass().getDeclaredField("driver");
            driverField.setAccessible(true);
            WebDriver driver = (WebDriver) driverField.get(testClass);

            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            String time = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            String path = "reports/screenshots/"
                    + result.getMethod().getMethodName()
                    + "_" + time + ".png";

            new File("reports/screenshots").mkdirs();
            src.renameTo(new File(path));

            extentTest.addScreenCaptureFromPath(path);

        } catch (Exception e) {
            extentTest.warning("⚠ Screenshot capture failed: " + e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().skip("⏭ Test Skipped");
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }
}
