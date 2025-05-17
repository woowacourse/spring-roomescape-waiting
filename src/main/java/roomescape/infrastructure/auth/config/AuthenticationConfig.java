package roomescape.infrastructure.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import roomescape.infrastructure.auth.jwt.JwtTokenProvider;

@Configuration
public class AuthenticationConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider(@Value("${jwt.secret_key}") String secretKey) {
        return new JwtTokenProvider(secretKey);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
