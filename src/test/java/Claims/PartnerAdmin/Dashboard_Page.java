package Claims.PartnerAdmin;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dashboard_Page {

    WebDriver driver;
    WebDriverWait wait;

    // Active Claims card (text anchor)
    private final By activeClaimsCard =
            By.xpath("//*[contains(normalize-space(),'Active Claims')]");

    // View Claims button (from your DOM)
    private final By viewClaimsBtn =
            By.xpath("//button[normalize-space()='View Claims']");

    public Dashboard_Page(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public int getActiveClaims() {

        WebElement card = wait.until(
                ExpectedConditions.visibilityOfElementLocated(activeClaimsCard)
        );

        String rawText = card.getText();
        System.out.println("ACTIVE CLAIMS CARD TEXT:\n" + rawText);

        Matcher matcher = Pattern.compile("\\b\\d+\\b").matcher(rawText);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }

        throw new RuntimeException("Active Claims value not found");
    }
    
 // Partner Referee list row
    private final By partnerRefereeRow =
            By.xpath("//div[contains(@class,'tableRow')]");

    public boolean isPartnerRefereeListDisplayed() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(partnerRefereeRow));
        return true;
    }
    public void waitForDashboardToLoad() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[normalize-space()='Partner Referees']")
        ));
    }



    public void clickViewClaimsForPartnerReferee() {

        By enabledViewClaimsBtn =
                By.xpath("//button[normalize-space()='View Claims' and not(@disabled)]");

        WebElement button = wait.until(
                ExpectedConditions.presenceOfElementLocated(enabledViewClaimsBtn)
        );

        // Scroll into view (IMPORTANT)
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", button);

        // Wait until clickable
        wait.until(ExpectedConditions.elementToBeClickable(button));

        System.out.println("Clicking ENABLED View Claims button");

        try {
            button.click();
        } catch (Exception e) {
            // Fallback for React button
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", button);
        }

        // Wait for navigation
        wait.until(ExpectedConditions.urlContains("claims"));
    }
}
