package com.automatization.signing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;

import static com.automatization.signing.util.ProccessHelper.*;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class SigningApplication {

    @Value("${app.web-driver.url}")
    private String webDriver;
    private final RestTemplate restTemplate = new RestTemplate();


    @PostConstruct
    public void init() {
        restTemplate.getForObject(String.format(URL_TELEGRAM,
                TOKEN_BOT,
                CHANNEL_PRIVATE,
                "¡DonnaBot iniciada!"),
                String.class);
        System.setProperty("webdriver.chrome.driver", webDriver);
    }

    public static void main(String[] args) {
        SpringApplication.run(SigningApplication.class, args);
    }

}
