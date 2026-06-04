package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.query.dto.PopularThemeQueryResult;

@JdbcTest
class JdbcReservationQueryDaoTest {

    private final JdbcTemplate jdbcTemplate;
    private final ReservationQueryDao reservationQueryDao;

    @Autowired
    JdbcReservationQueryDaoTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationQueryDao = new JdbcReservationQueryDao(jdbcTemplate);
    }

    @Test
    @DisplayName("인기 테마 조회는 집계 결과를 반환한다.")
    void queryPopularThemes_returnsThemes() {
        // given
        long timeId = createTime(LocalTime.of(10, 0));
        long themeId = createTheme("인기테마", "설명", "url");
        createReservation("브라운", LocalDate.of(2026, 5, 1), timeId, themeId);
        createReservation("콘니", LocalDate.of(2026, 5, 2), timeId, themeId);

        // when
        List<PopularThemeQueryResult> result = reservationQueryDao.queryPopularThemes(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3),
                10
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("인기테마");
    }

    private long createTime(LocalTime time) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", Time.valueOf(time));
        return jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = ?", Long.class, Time.valueOf(time));
    }

    private long createTheme(String name, String description, String thumbnailUrl) {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", name, description, thumbnailUrl);
        return jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = ?", Long.class, name);
    }

    private void createReservation(String name, LocalDate date, long timeId, long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id, theme_id, updated_at) VALUES (?, ?, ?, ?, ?)",
                name,
                date,
                timeId,
                themeId,
                java.sql.Timestamp.valueOf(date.atStartOfDay())
        );
    }
}
