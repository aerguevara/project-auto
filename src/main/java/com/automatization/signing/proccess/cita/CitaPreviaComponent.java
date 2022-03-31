package com.automatization.signing.proccess.cita;

import com.automatization.signing.AutoException;
import com.automatization.signing.model.Counter;
import com.automatization.signing.model.Person;
import com.automatization.signing.properties.PersonProperties;
import com.automatization.signing.repository.CounterRepository;
import com.automatization.signing.service.BotService;
import com.automatization.signing.util.ProccessHelper;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private WebDriver driverOriginal;
    private WebDriver remoteDriver;
    private CounterRepository counterRepository;
    @Value("${app.sleep-minute}")
    private int sleepMinute;

    public CitaPreviaComponent(PersonProperties personProperties,
                               BotService botService,
                               CounterRepository counterRepository) {
        this.personProperties = personProperties;
        this.botService = botService;
        this.counterRepository = counterRepository;
        this.driverOriginal = builderDriver();
        this.remoteDriver = builderRemoteDriver();
    }

    private static WebDriver builderRemoteDriver() {
        URL url = null;
        try {
            url = new URL("http://localhost:4444/wd/hub");
            ChromeOptions options = new ChromeOptions();
            options.addArguments("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.82 Safari/537.36");
            WebDriver remoteWebDriver = new RemoteWebDriver(url, options);
            remoteWebDriver.manage().window().setSize(new Dimension(1440, 900));
            return remoteWebDriver;
        } catch (MalformedURLException | UnreachableBrowserException e) {
            try {
                log.info("Ucurrio un error al iniciar la conexion al webdriver");
                Runtime.getRuntime().exec("docker start remote_1");
                ProccessHelper.pensarUnPoco();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return builderRemoteDriver();
        }
    }

    private static WebDriver builderDriver() {
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1440, 900));
        return driver;
    }

    public void iniciarProcesoCita() {
        boolean entry =
                getCounter()
                        .getBlockFail()
                        .stream()
                        .max(LocalDateTime::compareTo)
                        .map(this::compareMilisecondSleep)
                        .orElse(true);
        if (entry) {
            closeRemote();
            log.info("USANDO EL DRIVER ORIGINAL");
            findDateByWebDriver(driverOriginal, true);
        } else {
            log.info("SLEEP POR BLOQUEO EL ORIGINAL - USAMOS EL REMOTO MIENTRAS");
            findDateByWebDriver(remoteDriver, false);
        }


    }

    private void closeRemote() {
        try {
            remoteDriver.close();
            remoteDriver.quit();
        } catch (WebDriverException e) {
            log.info("OCURRIO UN ERROR AL CERRAR LA SESION {}", e.getMessage());
        }

    }

    private void findDateByWebDriver(WebDriver localDriver, boolean isLocal) {
        log.info("*****************************INICIAMOS EL PROCESO DE BUSQUEDA DE CITA*****************************");
        personProperties.getData()
                .stream()
                .findFirst()
                .ifPresent((Person person) -> {
                    try {
                        localDriver.manage().deleteAllCookies();
                        localDriver.navigate().to(urlCita);
                        stepOne(localDriver);
                        stepTwo(person, localDriver);
                        stepThree(localDriver);
                        stepFour(localDriver);
                    } catch (NoSuchElementException | ElementClickInterceptedException | AutoException e) {
                        log.info("MENSAJE DE ERROR {} ", e.getMessage());
                        Counter counter = getCounter();
                        counter.setLastModify(LocalDateTime.now());
                        counter.setFail(counter.getFail() + 1);
                        if (localDriver.getPageSource().contains("ERROR [500]") && isLocal) {
                            counter.getBlockFail().add(LocalDateTime.now());
                            remoteDriver = builderRemoteDriver();
                        }
                        counterRepository.save(counter);
                    }
                });
    }

    private boolean compareMilisecondSleep(LocalDateTime lastBlock) {
        long minute = ChronoUnit.MINUTES.between(lastBlock, LocalDateTime.now());
        return minute >= sleepMinute;

    }

    private Counter getCounter() {
        return counterRepository.findByActiveIsTrue()
                .orElseGet(() ->
                        Counter.builder()
                                .created(LocalDate.now())
                                .active(true)
                                .fail(0)
                                .success(0)
                                .successDate(new ArrayList<>())
                                .blockFail(new ArrayList<>())
                                .build());
    }

    private void stepFour(WebDriver driver) {
        log.info("RESOURCE PAGE DIFF ERROR 500 {}", driver.getPageSource());
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
        Counter counter = getCounter();
        counter.setLastModify(LocalDateTime.now());
        counter.getSuccessDate().add(counter.getLastModify());
        counter.setSuccess(counter.getSuccess() + 1);
        counterRepository.save(counter);
        // stepFiveBuilder(selectSede, person);
    }

    private void stepFiveBuilder(Select selectSede, Person person, WebDriver driverInProgress) {
        log.info("*****************************INICIAMOS FASE 5 DEL PROCESO DE SOLICITUD*****************************");
        log.info(driverInProgress.getPageSource());
        Optional<WebElement> selected = selectSede
                .getOptions()
                .stream()
                .filter(webElement -> !webElement.getText().contains("Seleccionar"))
                .findFirst();
        selected.ifPresent((WebElement webElement) -> {
            selectSede.selectByVisibleText(webElement.getText());
            driverInProgress.findElement(By.id("btnSiguiente")).click();
            driverInProgress.findElement(By.id("txtTelefonoCitado")).sendKeys(person.getPhone());
            driverInProgress.findElement(By.id("emailUNO")).sendKeys(person.getMail());
            driverInProgress.findElement(By.id("emailDOS")).sendKeys(person.getMail());
            log.info(driverInProgress.getPageSource());
            driverInProgress.findElement(By.id("btnSiguiente")).click();
            log.info("IMPRIMIENDO EN LOG LA PAGINA PARA CONOCER LOS ELEMENTOS DISPONIBLES");
            log.info(driverInProgress.getPageSource());
        });


    }

    private void stepThree(WebDriver driver) {
        try {
            driver.findElement(
                    By.xpath("//p[contains(text(),'En este momento no hay citas disponibles.')]"));
            throw new AutoException("No hay citas disponibles");
        } catch (NoSuchElementException noSuchElementException) {
            if (!driver.getPageSource().contains("ERROR [500]")) {
                log.error("SE ENCONTRARON CITAS DISPONIBLE", noSuchElementException);
            } else {
                throw new AutoException("No hay citas disponibles");
            }

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
        ProccessHelper.scrollDown(driver);
        driver.findElement(By.id("btnAceptar")).click();
        ProccessHelper.scrollDown(driver);
        driver.findElement(By.id("btnEntrar")).click();
    }

    public void sendResume() {
        Counter counter = getCounter();
        botService.sendNotification(
                MessageFormat.format("¡RESUMEN DEL DIA! En las ultimas 24 horas se encontraron {0} "
                                .concat("citas de {1} intentos."),
                        counter.getSuccess(),
                        counter.getFail()
                ),
                false);

        botService.sendNotificationUnique(
                MessageFormat.format("¡RESUMEN DEL DIA! En las ultimas 24 horas se encontrarón {0} "
                                .concat("citas de {1} intentos.\n\nPara más información entra en @NimbusGodBot."),
                        counter.getSuccess(),
                        counter.getFail()
                ));
        counter.setActive(false);
        counterRepository.save(counter);
    }


    public void sendActivityLog() {
        Counter counter = getCounter();
        botService.sendNotification(
                MessageFormat.format("¡REPORTE DE ACTIVIDAD! INTENTOS REALIZADOS {0} ",
                        counter.getFail()
                ), true);
    }
}
