package com.automatization.signing.proccess.cita;

import com.automatization.signing.AutoException;
import com.automatization.signing.Job.TelegramBotComponent;
import com.automatization.signing.model.Person;
import com.automatization.signing.properties.Counter;
import com.automatization.signing.properties.PersonProperties;
import com.automatization.signing.util.ProccessHelper;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
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

import static com.automatization.signing.util.ProccessHelper.*;


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
    private final TelegramBotComponent telegramBotComponent;

    public CitaPreviaComponent(PersonProperties personProperties,
                               Counter counter,
                               TelegramBotComponent telegramBotComponent) {
        this.personProperties = personProperties;
        this.counter = counter;
        this.restTemplate = new RestTemplate();
        this.driver = builderDriver();
        this.telegramBotComponent = telegramBotComponent;
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
        String sedeDisponible = selectSede
                .getOptions()
                .stream()
                .map(WebElement::getText)
                .filter(text -> !text.contains("Seleccionar"))
                .collect(Collectors.joining(" - "));

        restTemplate.getForObject(String.format(URL_TELEGRAM,
                TOKEN_BOT,
                CHANNEL_PRIVATE,
                MessageFormat.format("Hay citas disponibles para {0} en {1}",
                        "ASILO - PRIMERA CITA-provincia de Madrid"
                        , sedeDisponible)),
                String.class);

        stepFiveBuilder(selectSede, person);


    }

    private void stepFiveBuilder(Select selectSede, Person person) {
        telegramBotComponent.sendPhoto(ProccessHelper.takeScreenshot(driver), CHAT_ID_PERSONAL);
        log.info("*****************************INICIAMOS FASE 5 DEL PROCESO DE SOLICITUD*****************************");
        log.info(driver.getPageSource());
        Optional<WebElement> selected = selectSede
                .getOptions()
                .stream()
                .filter(webElement -> !webElement.getText().contains("Seleccionar"))
                .findFirst();
        selected.ifPresent((WebElement webElement) -> {
            restTemplate.getForObject(String.format(URL_TELEGRAM,
                    TOKEN_BOT,
                    CHANNEL_SPT,
                    MessageFormat.format("primer sede seleccionada {0}",
                            webElement.getText())),
                    String.class);
            selectSede.selectByVisibleText(webElement.getText());
            driver.findElement(By.id("btnSiguiente")).click();
            driver.findElement(By.id("txtTelefonoCitado")).sendKeys(person.getPhone());
            driver.findElement(By.id("emailUNO")).sendKeys(person.getMail());
            driver.findElement(By.id("emailDOS")).sendKeys(person.getMail());
            restTemplate.getForObject(String.format(URL_TELEGRAM,
                    TOKEN_BOT,
                    CHANNEL_SPT,
                    "Se ha logrado llenar la fase 5 del proceso"),
                    String.class);
            driver.findElement(By.id("btnSiguiente")).click();
            log.info("IMPRIMIENDO EN LOG LA PAGINA PARA CONOCER LOS ELEMENTOS DISPONIBLES");
            log.info(driver.getPageSource());

        });


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
                CHANNEL_PRIVATE,
                MessageFormat.format("¡RESUMEN DEL DIA! En las ultimas 24 horas se encontraron {0} "
                                .concat("citas de {1} intentos."),
                        counter.getSuccess(),
                        counter.getFail()
                )),
                String.class);
        resetCounter();
    }

    public void sendActivityLog() {
        restTemplate.getForObject(String.format(URL_TELEGRAM,
                TOKEN_BOT,
                CHANNEL_SPT,
                MessageFormat.format("Reporte de actividad, intentos fallidos:  {0} ",
                        counter.getFail()
                )),
                String.class);
    }

    private void resetCounter() {
        counter.setSuccess(0);
        counter.setFail(0);
    }
}
