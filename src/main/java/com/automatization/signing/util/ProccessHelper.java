package com.automatization.signing.util;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.*;

import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;


/**
 * @author Teams VectorITCGroup
 * @since 05/11/2020
 */
@Slf4j
public final class ProccessHelper {

    private ProccessHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static WebDriver getWebAndLogin(String url, String usr, String pass) {
        log.info("*****************************CARGAMOS EL NAVEGADOR*****************************");
        WebDriver driver = new ChromeDriver();
        driver.navigate().to(url);
        pensarUnPoco();
        driver.findElement(By.xpath("//input[@placeholder='Usuario']")).sendKeys(usr);
        driver.findElement(By.xpath("//input[@placeholder='Contraseña']")).sendKeys(pass);
        WebElement button = driver.findElement(By.xpath("//ion-button[@type='submit']"));
        button.click();
        button.click();

        return driver;
    }

    public static void pensarUnPoco() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException pepiotoerror) {
            log.error("ERROR AL PENSAR EN LA INMORTALIDAD DEL CANGREJO :c", pepiotoerror);
        }
    }

    public static File takeScreenshot(WebDriver webDriver) {
        pensarUnPoco();
        TakesScreenshot screenshot = (TakesScreenshot) webDriver;
        return screenshot.getScreenshotAs(OutputType.FILE);

    }

    public static void scrollDown(WebDriver webDriver) {
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript("window.scrollBy(0,document.body.scrollHeight)");
    }

}
