package Claims.Project;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import java.time.Duration;

public class BaseLogin {

    protected static WebDriver driver;
    protected static WebDriverWait wait;

    // Login config
    protected static final String LOGIN_URL = "https://dev-portal.claimsconcierge.io/login";
    protected static final String EMAIL = "superadmin@claimconcierge.com";
    protected static final String PASSWORD = "AmwhizTester@123";

    // Login XPaths (same as your working ones)
    protected static final By EMAIL_XPATH =
            By.xpath("//input[@placeholder='eg) john@gmail.com']");
    protected static final By PASSWORD_XPATH =
            By.xpath("//input[@placeholder='Password']");
    protected static final By LOGIN_BUTTON =
            By.xpath("//button[.//span[@class='p-button-label p-c']]");

    @BeforeClass(alwaysRun = true)
    public void loginOnce() {

        if (driver != null) return; // already logged in

        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();

            wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            driver.get(LOGIN_URL);

            wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_XPATH))
                    .sendKeys(EMAIL);
            driver.findElement(PASSWORD_XPATH).sendKeys(PASSWORD);
            driver.findElement(LOGIN_BUTTON).click();

            // wait for dashboard load
            wait.until(d -> !d.getCurrentUrl().contains("/login"));

            System.out.println("✅ Logged in successfully (BaseLogin)");
            
            if (LOGIN_URL!= null)
            {
            System.out.println("Network Error");	
            }
            else {
            	System.out.println("Navigated to URL");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("❌ Login failed in BaseLogin");
        }
    }
}
