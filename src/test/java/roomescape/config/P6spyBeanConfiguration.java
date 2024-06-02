package roomescape.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class P6spyBeanConfiguration {

    @Bean
    public P6spyEventListener p6spyCustomEventListener() {
        return new P6spyEventListener();
    }

    @Bean
    public P6spyFormatter p6spyCustomFormatter() {
        return new P6spyFormatter();
    }
}
