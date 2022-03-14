package com.automatization.signing.proccess.cita;

import com.automatization.signing.AutoException;
import com.automatization.signing.model.Counter;
import com.automatization.signing.model.Person;
import com.automatization.signing.properties.PersonProperties;
import com.automatization.signing.repository.CounterRepository;
import com.automatization.signing.service.BotService;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author Anyelo Reyes Guevara
 * @since 12/12/2021
 */
@Component
@Slf4j
public class CitaPreviaComponent {

    private final PersonProperties personProperties;
    private final BotService botService;
    @Value("${app.web.url-cita}")
    private String urlCita;
    private WebDriver driver;
    private CounterRepository counterRepository;

    public CitaPreviaComponent(PersonProperties personProperties,
                               BotService botService,
                               CounterRepository counterRepository) {
        this.personProperties = personProperties;
        this.driver = builderDriver();
        this.botService = botService;
        this.counterRepository = counterRepository;
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
                    } catch (AutoException e) {
                        log.info("MENSAJE DE ERROR :  {}",
                                e.getMessage());
                        Counter counter = getCounter();
                        counter.setLastModify(LocalDateTime.now());
                        counter.setFail(counter.getFail() + 1);
                        counterRepository.save(counter);
                    } catch (NoSuchElementException | ElementClickInterceptedException e) {
                        log.error("OCURRIO UN ERROR AL LLENAR LOS DATOS", e);
                        botService.sendNotification(
                                "OCURRIO UN ERROR AL LLENAR LOS DATOS",
                                true);
                    }

                });


    }

    private Counter getCounter() {
        return counterRepository.findByCreated(LocalDate.now())
                .orElseGet(() ->
                        Counter.builder()
                                .created(LocalDate.now())
                                .fail(0)
                                .success(0)
                                .successDate(new ArrayList<>())
                                .build());
    }

    private void stepFour(Person person, WebDriver driver) {
        Select selectSede = new Select(driver.findElement(By.id("idSede")));
        String sedeDisponible = selectSede
                .getOptions()
                .stream()
                .map(WebElement::getText)
                .filter(text -> !text.contains("Seleccionar"))
                .collect(Collectors.joining("\n- ", "\n- ", ""));
        botService.sendNotification(
                MessageFormat.format("<b>Hay citas disponibles para {0}</b> en: \n{1}",
                        "ASILO - PRIMERA CITA-provincia de Madrid"
                        , sedeDisponible),
                false);
        stepFiveBuilder(selectSede, person);
    }

    private void stepFiveBuilder(Select selectSede, Person person) {
        log.info("*****************************INICIAMOS FASE 5 DEL PROCESO DE SOLICITUD*****************************");
        log.info(driver.getPageSource());
        Optional<WebElement> selected = selectSede
                .getOptions()
                .stream()
                .filter(webElement -> !webElement.getText().contains("Seleccionar"))
                .findFirst();
        selected.ifPresent((WebElement webElement) -> {
            selectSede.selectByVisibleText(webElement.getText());
            driver.findElement(By.id("btnSiguiente")).click();
            driver.findElement(By.id("txtTelefonoCitado")).sendKeys(person.getPhone());
            driver.findElement(By.id("emailUNO")).sendKeys(person.getMail());
            driver.findElement(By.id("emailDOS")).sendKeys(person.getMail());
            log.info(driver.getPageSource());
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
            Counter counter = getCounter();
            counter.setLastModify(LocalDateTime.now());
            counter.getSuccessDate().add(counter.getLastModify());
            counter.setSuccess(counter.getSuccess() + 1);
            counterRepository.save(counter);
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
        Counter counter = getCounter();
        botService.sendNotification(
                MessageFormat.format("¡RESUMEN DEL DIA! En las ultimas 24 horas se encontraron {0} "
                                .concat("citas de {1} intentos."),
                        counter.getSuccess(),
                        counter.getFail()
                ), false);

    }


    public void sendActivityLog() {
        Counter counter = getCounter();
        botService.sendNotification(
                MessageFormat.format("¡REPORTE DE ACTIVIDAD! INTENTOS REALIZADOS {0} ",
                        counter.getFail()
                ), true);
    }
}
