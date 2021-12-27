package com.automatization.signing.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * @author Anyelo Reyes Guevara
 * @since 27/12/2021
 */
@Component
@Getter
@Setter
public class Counter {
    private int success;
    private int fail;

    public Counter() {
    }
}
