package roomescape.config;

import org.springframework.context.annotation.Bean;
import roomescape.auth.provider.JwtTokenProvider;

public class TestConfig {

    private static final String FAKE_KEY = "dkanakfdmfTmsmrkWkzldlqslekekemfghkdlxldlqslek=";
    private static final long FAKE_EXPIRATION_TIME = 1000000;

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(FAKE_KEY, FAKE_EXPIRATION_TIME);
    }
}
