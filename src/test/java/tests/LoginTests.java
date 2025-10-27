package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.Arguments;

import support.DriverHolder;
import support.ExtentWatcher;

@ExtendWith(ExtentWatcher.class)
public class LoginTests {

    public static class User {
        public String username;
        public String password;
        public String expect;
    }

    private WebDriver driver;

    private static String hubUrl() {
        return System.getProperty("hub",
            System.getenv().getOrDefault("HUB_URL", "http://20.125.51.186:4444"));
    }

    private static void pause(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }

    @AfterEach	
    void quit() {
        if (driver != null) driver.quit();
        DriverHolder.clear();
    }

    @ParameterizedTest(name = "{index} -> {0} on {1}")
    @MethodSource("userAndBrowser")
    void login_behaviour(User u, Capabilities browser) throws Exception {
        driver = new RemoteWebDriver(new URL(hubUrl()), browser);
        DriverHolder.set(driver);                           // <-- let Extent access the driver
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        driver.get("https://www.saucedemo.com/");
        pause(500);
        driver.findElement(By.id("user-name")).sendKeys(u.username);
        pause(500);
        driver.findElement(By.id("password")).sendKeys(u.password);
        pause(500);
        driver.findElement(By.id("login-button")).click();
        pause(500);

        String page = driver.getPageSource();
        assertTrue(page.contains(u.expect),
                "Did not find expected text: " + u.expect);
    }

    static Stream<User> users() throws Exception {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("testdata/users.json")) {
            if (is == null) throw new IllegalArgumentException("Missing testdata/users.json");
            User[] arr = new ObjectMapper().readValue(is, User[].class);
            return Arrays.stream(arr);
        }
    }

    static Stream<Arguments> userAndBrowser() throws Exception {
        List<User> users = users().toList();
        String pick = System.getProperty("browser", "both").toLowerCase();

        List<Capabilities> browsers = new ArrayList<>();
        if ("chrome".equals(pick) || "both".equals(pick))  browsers.add(new ChromeOptions());
        if ("firefox".equals(pick) || "both".equals(pick)) browsers.add(new FirefoxOptions());

        List<Arguments> out = new ArrayList<>();
        for (User u : users) for (Capabilities b : browsers) out.add(arguments(u, b));
        return out.stream();
    }
}
