package roomescape.wating.repository.jdbc;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.wating.domain.Waiting;

import java.sql.Time;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
class JdbcWaitingRepositoryTest {

    private static final LocalDateTime NOW = LocalDateTime.now(Clock.fixed(
            LocalDate.of(2026, 5, 8)
                    .atTime(10, 30)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant(),
            ZoneId.of("Asia/Seoul")
    ));

    @Autowired
    JdbcTemplate jdbcTemplate;

    JdbcWaitingRepository jdbcWaitingRepository;

    @BeforeEach
    void setUp() {
        jdbcWaitingRepository = new JdbcWaitingRepository(jdbcTemplate);
    }

    @Test
    void 날짜와_시간과_테마와_이름으로_대기를_등록할_수_있다() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~");
        Waiting waiting = Waiting.create(
                "코로구",
                NOW.plusDays(1).toLocalDate(),
                time,
                theme,
                NOW
        );

        //when
        final Long savedWaitingId = jdbcWaitingRepository.save(waiting);

        //then
        assertThat(savedWaitingId).isNotNull();
    }


    private ReservationTime insertReservationTime(final String startAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                startAt
        );
        return ReservationTime.of(1L, Time.valueOf(startAt).toLocalTime());
    }

    private Theme insertTheme(final String name, final String description, final String thumbnailUrl) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name,
                description,
                thumbnailUrl
        );

        return Theme.of(1L, name, description, thumbnailUrl);
    }
}
