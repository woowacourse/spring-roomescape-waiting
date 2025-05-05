package roomescape.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.auth.JwtTokenProvider;

@Configuration
public class AuthenticationConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider(@Value("jwt.secret_key") String secretKey) {
        return new JwtTokenProvider(secretKey);
    }
}
