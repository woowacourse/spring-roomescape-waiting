package roomescape.service.auth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public JwtTokenManager tokenProvider(JwtProperties jwtProperties) {
        return new JwtTokenManager(jwtProperties);
    }

    @Bean
    public TokenCookieManager tokenCookieManager() {
        return new TokenCookieManager();
    }
}
