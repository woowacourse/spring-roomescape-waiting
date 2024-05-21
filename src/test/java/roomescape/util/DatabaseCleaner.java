package roomescape.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleaner {
    @Autowired
    JdbcTemplate jdbcTemplate;
    public void initialize(){
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("TRUNCATE TABLE reservation");
        jdbcTemplate.update("TRUNCATE TABLE theme");
        jdbcTemplate.update("TRUNCATE TABLE member");
        jdbcTemplate.update("TRUNCATE TABLE reservation_time");
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY TRUE");
    }
}
