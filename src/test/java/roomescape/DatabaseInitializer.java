package roomescape;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clear() {
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("TRUNCATE TABLE orders");
        jdbcTemplate.update("TRUNCATE TABLE reservation");
        jdbcTemplate.update("TRUNCATE TABLE reservation_time");
        jdbcTemplate.update("TRUNCATE TABLE theme");
        jdbcTemplate.update("TRUNCATE TABLE reservation_waiting");
        jdbcTemplate.update("TRUNCATE TABLE member");
        jdbcTemplate.update("ALTER TABLE orders ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY TRUE");
    }
}
