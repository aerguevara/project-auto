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
    public static final String TOKEN_BOT = "836804862:AAFxSz2gpXfMdNZr7nyN79M8LuHS6CMLOcQ";
    public static final String URL_TELEGRAM =
            "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=html";

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

    public static void takeScreenshot(WebDriver webDriver, String nameImage, String directory) {
        pensarUnPoco();
        TakesScreenshot screenshot = (TakesScreenshot) webDriver;
        File file = screenshot.getScreenshotAs(OutputType.FILE);
        String path = directory.concat(nameImage).concat(".png");
        try {
            FileUtils.copyFile(file, new File(path));
        } catch (IOException e) {
            log.info("ERROR AL GUARDAR LA CAPTURA DEL NAVEGADOR");
        }
    }

}
