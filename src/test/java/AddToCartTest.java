import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        System.out.println("Opened Periplus");

        WebElement signInBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Sign In')]")
        ));
        signInBtn.click();

        login(email, password);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(),'Hi')]")
        ));
        System.out.println("Login success");

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.className("preloader")
        ));

        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("filter_name_desktop")
        ));
        searchBox.clear();
        searchBox.sendKeys("Let Them Theory");

        WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action*='Search'] button[type='submit']")
        ));
        searchBtn.click();

        wait.until(ExpectedConditions.urlContains("Search"));
        System.out.println("Search results loaded");

        List<WebElement> products = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        By.className("single-product")
                )
        );

        List<WebElement> availableProducts = new ArrayList<>();

        for (WebElement product : products) {
            String text = product.getText().toLowerCase();

            if (!text.contains("unavailable") && !text.contains("out of stock")) {
                availableProducts.add(product);
            }
        }

        Assert.assertTrue(availableProducts.size() > 0, "No available products found!");

        int index = new Random().nextInt(availableProducts.size());
        WebElement selectedProduct = availableProducts.get(index);

        System.out.println("Clicking AVAILABLE product index: " + index);

        System.out.println("Clicking product index: " + index);

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView(true);", selectedProduct
        );

        WebElement productLink = selectedProduct.findElement(
                By.xpath(".//a[contains(@href,'/p/')]")
        );

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("preloader")));
        wait.until(ExpectedConditions.elementToBeClickable(productLink));

        try {
            productLink.click();
        } catch (ElementClickInterceptedException e) {
            System.out.println("Click intercepted, pakai JS click");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", productLink);
        }

        wait.until(ExpectedConditions.urlContains("/p/"));
        System.out.println("Product detail page opened");

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
        System.out.println("Product added to cart");

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

        System.out.println("Cart verification passed");

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

        wait.until(driver -> !passwordInput.getAttribute("value").isEmpty());

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