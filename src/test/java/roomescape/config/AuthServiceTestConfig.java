package roomescape.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import roomescape.auth.service.out.TokenProvider;
import roomescape.auth.stub.StubTokenProvider;

@TestConfiguration
public class AuthServiceTestConfig {

    @Primary
    @Bean
    public TokenProvider stubTokenProvider() {
        return new StubTokenProvider();
    }
}
