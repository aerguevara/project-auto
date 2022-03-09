package com.automatization.signing.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;

/**
 * @author Teams VectorITCGroup
 * @since 05/11/2020
 */
@Slf4j
public final class ProccessHelper {

    public static final String CHANNEL_COMUN = "@cita_asilo_madrid";
    public static final String CHANNEL_SPT = "-1001652227932";
    public static final String CHANNEL_PRIVATE = "-1001775645268";
    public static final String TOKEN_BOT = "";
    public static final String URL_TELEGRAM =
            "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=html";
    public static final String CHAT_ID_PERSONAL = "795430222";

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
            Thread.sleep(1000);
        } catch (InterruptedException pepiotoerror) {
            log.error("ERROR AL PENSAR EN LA INMORTALIDAD DEL CANGREJO :c", pepiotoerror);
        }
    }

    public static File takeScreenshot(WebDriver webDriver) {
        pensarUnPoco();
        TakesScreenshot screenshot = (TakesScreenshot) webDriver;
        return screenshot.getScreenshotAs(OutputType.FILE);

    }

}
