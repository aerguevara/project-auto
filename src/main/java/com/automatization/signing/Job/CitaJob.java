package com.automatization.signing.Job;

import com.automatization.signing.proccess.cita.CitaPreviaComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Anyelo Reyes Guevara
 * @since 12/12/2021
 */
@Component
@Slf4j
public class CitaJob {

    private final CitaPreviaComponent citaPreviaComponent;

    public CitaJob(CitaPreviaComponent citaPreviaComponent) {
        this.citaPreviaComponent = citaPreviaComponent;
    }

    @Scheduled(fixedDelay = 60000)
    public void initProccess() {
        citaPreviaComponent.iniciarProcesoCita();
    }

        @Scheduled(cron = "${app.job.cron}")
    private void resume() {
        log.info("***************************** ENVIANDO RESUMEN *****************************");
        citaPreviaComponent.sendResume();
    }

}
