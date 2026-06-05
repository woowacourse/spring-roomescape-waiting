package roomescape.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class JdbcConfig {

    @Bean
    @Primary
    public ReadOnlyAwareJdbcTemplate readOnlyAwareJdbcTemplate(DataSource dataSource) {
        return new ReadOnlyAwareJdbcTemplate(dataSource);
    }
}
