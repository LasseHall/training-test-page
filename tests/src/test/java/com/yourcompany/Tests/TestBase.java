package com.yourcompany.Tests;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.testng.SauceOnDemandAuthenticationProvider;
import com.saucelabs.testng.SauceOnDemandTestListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.UnexpectedException;

/**
 * Simple TestNG test which demonstrates being instantiated via a DataProvider in order to supply multiple browser combinations.
 *
 * @author Neil Manvar
 */
public class TestBase  {

    public String buildTag = System.getenv("BUILD_TAG");

    public String username = System.getenv("SAUCE_USERNAME");

    public String accesskey = System.getenv("SAUCE_ACCESS_KEY");

    /**
     * ThreadLocal variable which contains the  {@link WebDriver} instance which is used to perform browser interactions with.
     */
    private ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();

    /**
     * ThreadLocal variable which contains the Sauce Job Id.
     */
    private ThreadLocal<String> sessionId = new ThreadLocal<String>();

    /**
     * DataProvider that explicitly sets the browser combinations to be used.
     *
     * @param testMethod
     * @return Two dimensional array of objects with browser, version, and platform information
     */
    @DataProvider(name = "hardCodedBrowsers", parallel = true)
    public static Object[][] sauceBrowserDataProvider(Method testMethod) throws JSONException {

        JSONArray array = new JSONArray(System.getenv("SAUCE_ONDEMAND_BROWSERS"));
        Object[][] browsers = new Object[array.length()][3];
        String device = "";
        for (int i = 0; i < array.length(); i++) {
            JSONObject browser = array.getJSONObject(i);
            try {
                device = browser.getString("device");
            } catch (org.json.JSONException e) {}
            if (browser.getString("os").toLowerCase().contains("ipad") || browser.getString("os").toLowerCase().contains("iphone") || browser.getString("os").toLowerCase().contains("ios"))
            {
                browsers[i] = new Object[]{"Safari", browser.getString("browser-version"), browser.getString("os"), device};
            } else if (browser.getString("os").toLowerCase().contains("android"))
            {
                browsers[i] = new Object[]{"Chrome", browser.getString("browser-version"), browser.getString("os"), device};
            }
            else {
                browsers[i] = new Object[]{browser.getString("browser"), browser.getString("browser-version"), browser.getString("os"), ""};
            }
        }

        JSONArray array2 = new JSONArray(System.getenv("SAUCE_ONDEMAND_RDC"));
        for (int i = 0; i < array2.length(); i++) {
            JSONObject browser = array2.getJSONObject(i);
            try {
                device = browser.getString("device");
            } catch (org.json.JSONException e) {}
            if (browser.getString("os").toLowerCase().contains("ipad") || browser.getString("os").toLowerCase().contains("iphone") || browser.getString("os").toLowerCase().contains("ios"))
            {
                browsers[i] = new Object[]{"Safari", browser.getString("browser-version"), browser.getString("os"), device};
            } else if (browser.getString("os").toLowerCase().contains("android"))
            {
                browsers[i] = new Object[]{"Chrome", browser.getString("browser-version"), browser.getString("os"), device};
            }
            else {
                browsers[i] = new Object[]{browser.getString("browser"), browser.getString("browser-version"), browser.getString("os"), ""};
            }
        }
                /*new Object[]{"MicrosoftEdge", "14.14393", "Windows 10"},
                new Object[]{"firefox", "49.0", "Windows 10"},
                new Object[]{"internet explorer", "11.0", "Windows 7"},
                new Object[]{"safari", "10.0", "OS X 10.11"},
                new Object[]{"chrome", "54.0", "OS X 10.10"},
                new Object[]{"firefox", "latest-1", "Windows 7"},*/
        return browsers;
    }

    /**
     * @return the {@link WebDriver} for the current thread
     */
    public WebDriver getWebDriver() {
        return webDriver.get();
    }

    /**
     *
     * @return the Sauce Job id for the current thread
     */
    public String getSessionId() {
        return sessionId.get();
    }

    /**
     * Constructs a new {@link RemoteWebDriver} instance which is configured to use the capabilities defined by the browser,
     * version and os parameters, and which is configured to run against ondemand.saucelabs.com, using
     * the username and access key populated by the {@link #authentication} instance.
     *
     * @param browser Represents the browser to be used as part of the test run.
     * @param version Represents the version of the browser to be used as part of the test run.
     * @param os Represents the operating system to be used as part of the test run.
     * @param methodName Represents the name of the test case that will be used to identify the test on Sauce.
     * @return
     * @throws MalformedURLException if an error occurs parsing the url
     */
    protected void createDriver(String browser, String version, String os, String deviceName, String methodName)
            throws MalformedURLException, UnexpectedException {
        DesiredCapabilities capabilities = new DesiredCapabilities();



        if (buildTag != null) {
            capabilities.setCapability("build", buildTag);
        }

        // Launch remote browser and set it as the current thread
        if (deviceName.contains("59CE6AD64AE24CC5B1451EB76B833F2E")) {
            // set desired capabilities to launch appropriate browser on Sauce
            capabilities.setCapability(CapabilityType.BROWSER_NAME, browser);
            capabilities.setCapability("deviceName", version);
            capabilities.setCapability("platformName", os);
            capabilities.setCapability("testobject_api_key", deviceName);
            capabilities.setCapability("name", methodName);
            capabilities.setCapability("tunnelIdentifier", "MyRDCTunnel");
            webDriver.set(new RemoteWebDriver(
                    new URL("https://eu1.appium.testobject.com/wd/hub"),
                    capabilities));
        } else {
            // set desired capabilities to launch appropriate browser on Sauce
            capabilities.setCapability(CapabilityType.BROWSER_NAME, browser);
            capabilities.setCapability(CapabilityType.VERSION, version);
            capabilities.setCapability("platformVersion", version);
            capabilities.setCapability(CapabilityType.PLATFORM, os);
            capabilities.setCapability("deviceName", deviceName);
            capabilities.setCapability("name", methodName);
            webDriver.set(new RemoteWebDriver(
                    new URL("https://" + username + ":d3c745e2-9030-4c0c-8cfc-9538a6d8e895@ondemand.saucelabs.com:443/wd/hub"),
                    capabilities));
        }


        // set current sessionId
        String id = ((RemoteWebDriver) getWebDriver()).getSessionId().toString();
        sessionId.set(id);
    }

    /**
     * Method that gets invoked after test.
     * Dumps browser log and
     * Closes the browser
     */
    @AfterMethod
    public void tearDown(ITestResult result) throws Exception {
        ((JavascriptExecutor) webDriver.get()).executeScript("sauce:job-result=" + (result.isSuccess() ? "passed" : "failed"));
        webDriver.get().quit();
    }

    protected void annotate(String text) {
        ((JavascriptExecutor) webDriver.get()).executeScript("sauce:context=" + text);
    }
}
