import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import java.time.Duration;

public class AddToCartTest {

    WebDriver driver;
    WebDriverWait wait;

    String email = System.getenv("EMAIL");
    String password = System.getenv("PASSWORD");

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @Test
    public void testAddProductToCart() throws InterruptedException {

        driver.manage().deleteAllCookies();

        driver.get("https://www.periplus.com/");

        WebElement signInBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Sign In')]")
        ));
        signInBtn.click();

        login(email, password);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(),'Hi')]")
        ));

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.className("preloader")
        ));

        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("filter_name_desktop")
        ));
        searchBox.clear();
        searchBox.sendKeys("Harry Potter");

        WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action*='Search'] button[type='submit']")
        ));
        searchBtn.click();

        wait.until(ExpectedConditions.urlContains("Search"));

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.className("single-product")
        ));

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.className("preloader")
        ));

        WebElement product = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("(//div[@class='single-product'])[5]//a[contains(@href,'/p/')]")
                )
        );
        product.click();

        wait.until(ExpectedConditions.urlContains("/p/"));

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.className("preloader")
        ));

        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.btn-add-to-cart")
        ));

        addToCartBtn.click();

        WebElement successPopup = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Success add to cart')]")
        ));

        Assert.assertTrue(successPopup.isDisplayed(), "Add to cart failed!");

        wait.until(ExpectedConditions.invisibilityOf(successPopup));

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.className("preloader")
        ));

        WebElement cartIcon = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("a[href*='checkout/cart']")
        ));

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cartIcon);

        wait.until(ExpectedConditions.urlContains("checkout/cart"));

        WebElement cartItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("row-cart-product")
        ));

        WebElement checkoutBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[@onclick='beginCheckout()']")
        ));

        Assert.assertTrue(cartItem.isDisplayed(), "Cart is empty!");
        Assert.assertTrue(checkoutBtn.isDisplayed(), "Checkout button not visible!");

    }

    private void login(String email, String password) {

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.name("email")
        ));
        emailInput.clear();
        emailInput.sendKeys(email);

        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.name("password")
        ));
        passwordInput.clear();
        passwordInput.sendKeys(password);

        wait.until(driver -> passwordInput.getAttribute("value").length() > 0);

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button | //input[@type='submit']")
        ));

        loginButton.click();
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}