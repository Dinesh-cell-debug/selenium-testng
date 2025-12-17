package Claims.Project;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Listeners(Claims.Project.listeners.ExtentListener.class)
public class DashboardTest {

    private WebDriver driver;
    private WebDriverWait wait;

    // ================= CONFIG =================
    private static final String LOGIN_URL =
            "https://dev-portal.claimsconcierge.io/login";

    private static final String EMAIL =
            "superadmin@claimconcierge.com";
    private static final String PASSWORD =
            "AmwhizTester@123";

    private static final String BASE_API_URL =
            "https://dev-api-portal.claimsconcierge.io";
    private static final String DASHBOARD_API =
            "/api/v1/companies/stats";

    // ================= LOGIN LOCATORS =================
    private static final By EMAIL_INPUT =
            By.xpath("//input[@placeholder='eg) john@gmail.com']");
    private static final By PASSWORD_INPUT =
            By.xpath("//input[@placeholder='Password']");
    private static final By LOGIN_BUTTON =
            By.xpath("//span[normalize-space()='Login']");

    // ================= DASHBOARD LOCATORS =================
    private static final By DASHBOARD_CONTAINER =
            By.xpath("//span[normalize-space()='Dashboard']");

    private static final By ACTIVE_CLAIMS =
            By.xpath("//p[normalize-space()='Active Claims']/following-sibling::p");

    private static final By COMPLETED_CLAIMS =
            By.xpath("//p[normalize-space()='Completed Claims']/following-sibling::p");

    // ================= PARTNER LIST =================
    private static final By VIEW_ALL_BUTTON =
            By.xpath("//a[contains(text(),'View')] | //button[contains(text(),'View')]");

    private static final By SEARCH_FIELD =
            By.xpath("//input[contains(@class,'searchInput')]");

    // ✅ ONLY partner name h3 inside each row
    private static final By PARTNER_ROWS =
            By.xpath("//div[contains(@class,'tableRowCard')]//h3");

    private static final By EMPTY_CARD =
            By.xpath("//*[contains(text(),'Nothing crashed') or contains(text(),'No data')]");

    private static final By PARTNER_LOGIN_BUTTON =
            By.xpath("(//button[normalize-space()='Login'])[1]");

    // ================= SETUP =================
    @BeforeClass(alwaysRun = true)
    public void setupAndLogin() {

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        driver.get(LOGIN_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT))
                .sendKeys(EMAIL);
        driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(DASHBOARD_CONTAINER));
    }

    // ================= TEST 1 =================
    @Test(priority = 1)
    public void TC_01_VerifyDashboardPageLoaded() {
        Assert.assertTrue(driver.getCurrentUrl().contains("dashboard"),
                "Dashboard page not loaded");
    }

    // ================= TEST 2 =================
    @Test(priority = 2)
    public void TC_02_VerifyDashboardDataLoaded() {
        waitForDashboardData();
        Assert.assertTrue(getNumber(ACTIVE_CLAIMS) >= 0);
        Assert.assertTrue(getNumber(COMPLETED_CLAIMS) >= 0);
    }

    // ================= TEST 3 =================
    @Test(priority = 3)
    public void TC_03_VerifyDashboardUIvsBackendMatch() {

        waitForDashboardData();

        RestAssured.baseURI = BASE_API_URL;

        String authToken = getAuthCookie();
        Assert.assertNotNull(authToken, "Auth cookie not found");

        Response response =
                RestAssured.given()
                        .cookie("_cc_auth", authToken)
                        .accept("application/json")
                        .get(DASHBOARD_API);

        Assert.assertEquals(response.statusCode(), 200);

        int uiActive = getNumber(ACTIVE_CLAIMS);
        int uiCompleted = getNumber(COMPLETED_CLAIMS);

        int apiActive =
                Integer.parseInt(response.jsonPath().getString("data.activeClaims"));
        int apiCompleted =
                Integer.parseInt(response.jsonPath().getString("data.completedClaims"));

        Assert.assertEquals(uiActive, apiActive, "Active claims mismatch");
        Assert.assertEquals(uiCompleted, apiCompleted, "Completed claims mismatch");
    }

    // ================= TEST 4 =================
    @Test(priority = 4)
    public void TC_04_VerifyViewAllNavigation() {
        navigateToPartnerListIfNeeded();
        Assert.assertTrue(driver.getCurrentUrl().contains("partners"));
    }

    // ================= TEST 5 =================
    @Test(priority = 5)
    public void TC_05_VerifyInvalidSearchShowsEmpty() {
        navigateToPartnerListIfNeeded();
        verifyInvalidSearch("INVALID_SEARCH_123");
    }

    // ================= TEST 6 =================
    @Test(priority = 6)
    public void TC_06_VerifyValidSearchShowsCorrectResult() {
        navigateToPartnerListIfNeeded();
        verifyValidSearch("HTC");
    }

    // ================= TEST 7 =================
    @Test(priority = 7)
    public void TC_07_VerifyPartnerLoginNavigation() {
        wait.until(ExpectedConditions.elementToBeClickable(PARTNER_LOGIN_BUTTON)).click();
        Assert.assertTrue(wait.until(d -> d.getCurrentUrl().contains("partner")));
    }

    // ================= SEARCH HELPERS =================

    private void verifyInvalidSearch(String invalidText) {

        WebElement search = wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_FIELD));
        search.clear();
        search.sendKeys(invalidText);

        boolean isEmpty = wait.until(d ->
                d.findElements(EMPTY_CARD).size() > 0
                        || d.findElements(PARTNER_ROWS).isEmpty()
        );

        Assert.assertTrue(isEmpty,
                "❌ Invalid search should show empty result");
    }

    private void verifyValidSearch(String validText) {

        WebElement search = wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_FIELD));
        search.clear();
        search.sendKeys(validText);

        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(PARTNER_ROWS, 0));

        List<WebElement> partners = driver.findElements(PARTNER_ROWS);

        boolean matched = false;

        for (WebElement partner : partners) {
            if (!partner.isDisplayed()) continue;

            String name = partner.getText().trim();

            if (name.equalsIgnoreCase(validText)) {
                matched = true;
                break;
            }
        }

        Assert.assertTrue(matched,
                "❌ Search mismatch. Expected partner: " + validText);
    }

    // ================= UTILS =================

    private void waitForDashboardData() {
        wait.until(d -> {
            try {
                return !driver.findElement(ACTIVE_CLAIMS).getText().trim().isEmpty();
            } catch (Exception e) {
                return false;
            }
        });
    }

    private int getNumber(By locator) {
        String text = wait.until(ExpectedConditions.visibilityOfElementLocated(locator))
                .getText().replaceAll("[^0-9]", "");
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    private String getAuthCookie() {
        for (Cookie cookie : driver.manage().getCookies()) {
            if ("_cc_auth".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void navigateToPartnerListIfNeeded() {
        if (!driver.getCurrentUrl().contains("partners")) {
            wait.until(ExpectedConditions.elementToBeClickable(VIEW_ALL_BUTTON)).click();
            wait.until(ExpectedConditions.urlContains("partners"));
        }
    }

    // ================= SCREENSHOT =================
    @AfterMethod(alwaysRun = true)
    public void screenshotOnFailure(ITestResult result) {
        if (ITestResult.FAILURE == result.getStatus()) {
            try {
                new File("screenshots").mkdirs();
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String time = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                FileUtils.copyFile(src,
                        new File("screenshots/" + result.getName() + "_" + time + ".png"));
            } catch (Exception ignored) {}
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
