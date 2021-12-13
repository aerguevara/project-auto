package com.automatization.signing.proccess.cita;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static com.automatization.signing.util.ProccessHelper.CHANNEL_COMUN;
import static com.automatization.signing.util.ProccessHelper.TOKEN_BOT;
import static com.automatization.signing.util.ProccessHelper.URL_TELEGRAM;
import static com.automatization.signing.util.ProccessHelper.takeScreenshot;


/**
 * @author Anyelo Reyes Guevara
 * @since 12/12/2021
 */
@Component
@Slf4j
public class CitaPreviaComponent {
    @Value("${app.web.url-cita}")
    private String urlCita;

    @Value("${app.directory.screen}")
    private String directorySreen;


    private RestTemplate restTemplate;

    public CitaPreviaComponent() {
        restTemplate = new RestTemplate();
    }

    public void iniciarProcesoCita() {
        log.info("*****************************INICIAMOS EL PROCESO DE BUSQUEDA DE CITA*****************************");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1440, 900));
        try {
            driver.navigate().to(urlCita);
            Select select = new Select(driver.findElement(By.id("tramiteGrupo[1]")));
            select.selectByValue("4104");
            log.info("Tramite Seleccionado");
            driver.findElement(By.id("btnAceptar")).click();
            driver.findElement(By.id("btnEntrar")).click();
            driver.findElement(By.id("rdbTipoDocPas")).click();
            driver.findElement(By.id("txtIdCitado")).sendKeys("C02370733");
            driver.findElement(By.id("txtDesCitado")).sendKeys("JAMILETH REYES GUEVARA");
            driver.findElement(By.id("txtAnnoCitado")).sendKeys("1992");
            Select selectNa = new Select(driver.findElement(By.id("txtPaisNac")));
            selectNa.selectByValue("236");
            log.info("Campos de datos relleno");
            driver.findElement(By.id("btnEnviar")).click();
            driver.findElement(By.id("btnEnviar")).click();
            driver.findElement(
                    By.xpath("//p[contains(text(),'En este momento no hay citas disponibles.')]"));
            log.info("No Hay citas");
        } catch (NoSuchElementException noSuchElementException) {
            log.error("error", noSuchElementException);
            restTemplate.getForObject(String.format(URL_TELEGRAM,
                            TOKEN_BOT,
                            CHANNEL_COMUN, "hay citas disponibles"),
                    String.class);
            takeScreenshot(driver, "EV_CITA_DISPONIBLE", directorySreen);
        } catch (ElementClickInterceptedException e) {
            log.info("Error al intentar darle click a un elemento, cerramos el navegador: {}", e.getMessage());
        }
        driver.close();
    }
}
