package Claims.PartnerAdmin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Login_Page {

    WebDriver driver;

    // ✅ Locators must be CLASS LEVEL
    By email = By.xpath("//input[@placeholder='eg) john@gmail.com']");
    By password = By.xpath("//input[@placeholder='Password']");
    By loginBtn = By.xpath("//span[normalize-space()='Login']");

    // ✅ ONLY ONE constructor (no return type)
    public Login_Page(WebDriver driver) {
        this.driver = driver;
    }

    // ✅ Reusable login method
    public void login(String user, String pass) {
        driver.findElement(email).sendKeys(user);
        driver.findElement(password).sendKeys(pass);
        driver.findElement(loginBtn).click();
    }
}
