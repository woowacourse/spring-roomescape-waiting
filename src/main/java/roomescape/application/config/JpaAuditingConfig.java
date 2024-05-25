package roomescape.application.config;

import java.time.Clock;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "customDateTimeProvider")
public class JpaAuditingConfig {

    @Autowired
    private Clock clock;

    @Bean
    public DateTimeProvider customDateTimeProvider() {
        return () -> Optional.of(clock.instant());
    }
}
