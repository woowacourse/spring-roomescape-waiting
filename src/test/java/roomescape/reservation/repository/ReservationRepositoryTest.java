package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.domain.ReservationSlot;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationFactory;
import roomescape.reservationtime.repository.JdbcReservationTimeRepository;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.theme.repository.ThemeRepository;

@JdbcTest
@Import({JdbcReservationRepository.class, JdbcReservationTimeRepository.class, JdbcThemeRepository.class,
        ReservationFactory.class})
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationFactory reservationFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long futureReservationId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('14:00', '15:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('18:00', '19:00')");
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url, price) VALUES ('테마A', '설명A', 'https://a.com', 10000)");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', '2099-12-01', 1, 1)");
        futureReservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
    }

    @Test
    @DisplayName("예약 저장 성공")
    void 예약_저장_성공() {
        Reservation saved = reservationRepository.save(
                reservationFactory.create("현미밥", new ReservationSlot(
                        LocalDate.now().plusDays(1),
                        timeRepository.findById(1L).get(),
                        themeRepository.findById(1L).get())));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("예약 삭제 성공")
    void 예약_삭제_성공() {
        reservationRepository.deleteById(futureReservationId);
        LocalDate date = LocalDate.of(2099, 12, 1);
        assertThat(timeRepository.findAvailableByDateAndThemeId(date, 1L)).hasSize(3);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        Clock clock() {
            return Clock.fixed(
                    LocalDate.now().atTime(14, 0).atZone(ZoneId.systemDefault()).toInstant(),
                    ZoneId.systemDefault()
            );
        }
    }
}
