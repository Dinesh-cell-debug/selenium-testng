package Claims.PartnerAdmin;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.*;

import java.time.Duration;

public class Base_Test {

    protected WebDriver driver;

    @BeforeMethod
    public void setup() {

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        driver.get("https://dev-portal.claimsconcierge.io/login");

        // ✅ Login before every test
        Login_Page login = new Login_Page(driver);
        login.login("cherathamw@proton.cc", "Welcome@2523");
    }

    @AfterMethod
    public void tearDown() {

        try {
            // Optional: Logout if button exists
            // driver.findElement(By.xpath("//span[text()='Logout']")).click();
        } catch (Exception ignored) {}

        if (driver != null) {
            driver.quit();
        }
    }
}
