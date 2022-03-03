package com.automatization.signing.proccess.cita;

import com.automatization.signing.AutoException;
import com.automatization.signing.model.Person;
import com.automatization.signing.properties.Counter;
import com.automatization.signing.properties.PersonProperties;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.automatization.signing.util.ProccessHelper.CHANNEL_COMUN;
import static com.automatization.signing.util.ProccessHelper.CHANNEL_SPT;
import static com.automatization.signing.util.ProccessHelper.TOKEN_BOT;
import static com.automatization.signing.util.ProccessHelper.URL_TELEGRAM;


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

    private Counter counter;


    private final RestTemplate restTemplate;
    private final PersonProperties personProperties;
    private WebDriver driver;

    public CitaPreviaComponent(PersonProperties personProperties,
                               Counter counter) {
        this.personProperties = personProperties;
        this.counter = counter;
        this.restTemplate = new RestTemplate();
        this.driver = builderDriver();
    }

    private static WebDriver builderDriver() {
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1440, 900));
        return driver;
    }

    public void iniciarProcesoCita() {
        log.info("*****************************INICIAMOS EL PROCESO DE BUSQUEDA DE CITA*****************************");
        personProperties.getData()
                .stream()
                .findFirst()
                .ifPresent((Person person) -> {

                    try {
                        driver.manage().deleteAllCookies();
                        driver.navigate().to(urlCita);
                        stepOne(driver);
                        stepTwo(person, driver);
                        stepThree(driver);
                        stepFour(person, driver);
                        counter.setSuccess(counter.getSuccess() + 1);
                    } catch (AutoException e) {
                        log.info("mensaje de error :  {}",
                                e.getMessage());
                        counter.setFail(counter.getFail() + 1);
                    }

                });


    }

    private void stepFour(Person person, WebDriver driver) {
        Select selectSede = new Select(driver.findElement(By.id("idSede")));
        Optional<WebElement> selected = selectSede
                .getOptions()
                .stream()
                .filter(webElement -> !webElement.getText().contains("Seleccionar"))
                .findFirst();
        String sedeDisponible = selectSede
                .getOptions()
                .stream()
                .map(WebElement::getText)
                .filter(text -> !text.contains("Seleccionar"))
                .collect(Collectors.joining(" - "));

        restTemplate.getForObject(String.format(URL_TELEGRAM,
                        TOKEN_BOT,
                        CHANNEL_COMUN,
                        MessageFormat.format("Hay citas disponibles para {0} en {1}",
                                "ASILO - PRIMERA CITA-provincia de Madrid"
                                , sedeDisponible)),
                String.class);
        selected.ifPresent((WebElement webElement) ->
                restTemplate.getForObject(String.format(URL_TELEGRAM,
                                TOKEN_BOT,
                                CHANNEL_SPT,
                                MessageFormat.format("primer sede seleccionada {0}",
                                        webElement.getText())),
                        String.class));


    }

    private void stepThree(WebDriver driver) {
        try {
            driver.findElement(
                    By.xpath("//p[contains(text(),'En este momento no hay citas disponibles.')]"));
            throw new AutoException("No hay citas disponibles");
        } catch (NoSuchElementException noSuchElementException) {
            log.error("SE ENCONTRARON CITAS DISPONIBLE", noSuchElementException);
        }

    }

    private void stepTwo(Person person, WebDriver driver) {
        driver.findElement(By.id("rdbTipoDocPas")).click();
        driver.findElement(By.id("txtIdCitado")).sendKeys(person.getPassport());
        driver.findElement(By.id("txtDesCitado")).sendKeys(person.getName());
        driver.findElement(By.id("txtAnnoCitado")).sendKeys(person.getYearOfBirth());
        Select selectNa = new Select(driver.findElement(By.id("txtPaisNac")));
        selectNa.selectByValue(person.getNationality());
        log.info("Campos de datos relleno");
        driver.findElement(By.id("btnEnviar")).click();
        driver.findElement(By.id("btnEnviar")).click();
    }

    private void stepOne(WebDriver driver) {
        Select select = new Select(driver.findElement(By.id("tramiteGrupo[1]")));
        select.selectByValue("4104");
        log.info("Tramite Seleccionado");
        driver.findElement(By.id("btnAceptar")).click();
        driver.findElement(By.id("btnEntrar")).click();
    }

    public void sendResume() {
        restTemplate.getForObject(String.format(URL_TELEGRAM,
                        TOKEN_BOT,
                        CHANNEL_COMUN,
                        MessageFormat.format("¡RESUMEN DEL DIA! En las ultimas 24 horas se encontraron {0} "
                                        .concat("citas de {1} intentos."),
                                counter.getSuccess(),
                                counter.getFail()
                        )),
                String.class);
        resetCounter();
    }

    private void resetCounter() {
        counter.setSuccess(0);
        counter.setFail(0);
    }
}
