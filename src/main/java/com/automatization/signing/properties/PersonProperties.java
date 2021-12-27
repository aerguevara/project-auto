package com.automatization.signing.properties;

import com.automatization.signing.model.Person;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Anyelo Reyes Guevara
 * @since 17/12/2021
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "person")
public class PersonProperties {

    private List<Person> data;
}
