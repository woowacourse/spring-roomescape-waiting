package roomescape.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
public class ReservationTableTest {

    @Autowired
    public JdbcTemplate jdbcTemplate;

    @DisplayName("reservation 스키마 구조 확인")
    @Test
    void reservation_schema() {
        // given
        // when
        // then
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES ('10:00')");
        jdbcTemplate.update("INSERT INTO theme(name, description, thumbnail) VALUES ('Theme 1', '테마1 설명','썸네일')");
        jdbcTemplate.update("INSERT INTO users (role, name, email, password) VALUES ('ROLE_MEMBER', 'n1', 'e1', 'p1')");
        jdbcTemplate.update(
                "INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES ('2025-11-11', 1L, 1L, 1L)");

    }
}
