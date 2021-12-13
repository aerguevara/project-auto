package com.automatization.signing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class SigningApplication {

    @Value("${app.web-driver.url}")
    private String webDriver;


    @PostConstruct
    public void init() {
        System.setProperty("webdriver.chrome.driver", webDriver);
    }

    public static void main(String[] args) {
        SpringApplication.run(SigningApplication.class, args);
    }

}
