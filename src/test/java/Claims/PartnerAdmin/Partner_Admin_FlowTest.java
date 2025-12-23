package Claims.PartnerAdmin;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
@Listeners(Claims.Project.listeners.ExtentListener.class)

public class Partner_Admin_FlowTest extends Base_Test {

    @Test(priority = 1)
    public void verifyDashboardActiveClaimsCount() {

        Dashboard_Page dashboard = new Dashboard_Page(driver);

        int activeClaims = dashboard.getActiveClaims();
        System.out.println("Active Claims Count: " + activeClaims);

        Assert.assertTrue(activeClaims >= 0,
                "Active claims count is invalid");
    }

    @Test(priority = 2)
    public void verifyPartnerRefereeList() {

        Dashboard_Page dashboard = new Dashboard_Page(driver);

        Assert.assertTrue(
                dashboard.isPartnerRefereeListDisplayed(),
                "Partner Referee list is not displayed"
        );
    }

    @Test(priority = 3)
    public void verifyViewClaimsNavigation() {

        Dashboard_Page dashboard = new Dashboard_Page(driver);
        dashboard.clickViewClaimsForPartnerReferee();

        Assert.assertTrue(
                driver.getCurrentUrl().toLowerCase().contains("claims"),
                "View Claims navigation failed"
        );
    }

    @Test(priority = 4)
    public void verifyRecentClaimsListAndStatus() {

        Dashboard_Page dashboard = new Dashboard_Page(driver);
        dashboard.clickViewClaimsForPartnerReferee();

        Claims_Page claims = new Claims_Page(driver);

        Assert.assertTrue(
                claims.isClaimDisplayed(),
                "Recent Claims list is not displayed"
        );

        Assert.assertEquals(
                claims.getClaimStatus(),
                "Initiated",
                "Claim status is incorrect"
        );
    }
}
