package roomescape.infrastructure.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SecurityConfig {

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new PasswordEncoder();
    }
}
