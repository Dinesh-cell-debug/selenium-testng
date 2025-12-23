package Claims.PartnerAdmin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Claims_Page {

    WebDriver driver;
    WebDriverWait wait;

    // First claim row
    private final By firstClaimRow =
            By.xpath("//table/tbody/tr[1]");

    // Claim Status chip (from your DOM)
    private final By claimStatus =
            By.xpath("//td[@data-pc-section='bodycell']//span[contains(@class,'chip')]");

    public Claims_Page(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public boolean isClaimDisplayed() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstClaimRow));
        return true;
    }

    public String getClaimStatus() {
        String status = wait.until(
                ExpectedConditions.visibilityOfElementLocated(claimStatus)
        ).getText().trim();

        System.out.println("Claim Status found: " + status);
        return status;
    }
}
