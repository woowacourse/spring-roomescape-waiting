package roomescape.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleaner {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseCleaner(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void cleanUp() {
        clearReservation();
        clearTime();
        clearTheme();
        clearMember();
    }

    private void clearReservation() {
        jdbcTemplate.update("delete from reservation");
        jdbcTemplate.update("alter table reservation alter column id restart with 1");
    }

    private void clearTime() {
        jdbcTemplate.update("delete from reservation_time");
        jdbcTemplate.update("alter table reservation_time alter column id restart with 1");
    }

    private void clearTheme() {
        jdbcTemplate.update("delete from theme");
        jdbcTemplate.update("alter table theme alter column id restart with 1");
    }

    private void clearMember() {
        jdbcTemplate.update("delete from member");
        jdbcTemplate.update("alter table member alter column id restart with 1");
    }
}
