package roomescape.test_config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestTimeManagerConfig {

    @Bean
    @Primary
    public MutableTimeManager mutableTimeManager() {
        return new MutableTimeManager();
    }
}
