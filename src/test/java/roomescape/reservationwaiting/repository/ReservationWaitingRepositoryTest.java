package roomescape.reservationwaiting.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
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
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.domain.ReservationWaitingFactory;

@JdbcTest
@Import({JdbcReservationWaitingRepository.class, JdbcReservationRepository.class, ReservationWaitingFactory.class})
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationWaitingRepositoryTest {

    @Autowired
    private JdbcReservationWaitingRepository jdbcReservationWaitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationWaitingFactory reservationWaitingFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Reservation pastReservation;
    private Reservation futureReservation;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마A', '설명A', 'https://a.com')");

        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', ?, 1, 1)",
                LocalDate.now().minusDays(1));
        Long pastReservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);

        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', '2099-12-01', 1, 1)");
        Long futureReservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);

        pastReservation = reservationRepository.findById(pastReservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        futureReservation = reservationRepository.findById(futureReservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @Test
    @DisplayName("예약 대기 신청에 성공한다.")
    void 예약_대기_성공() {
        ReservationWaiting saved = jdbcReservationWaitingRepository.save(
                reservationWaitingFactory.create("현미밥", futureReservation));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("이미 지난 시간과 날짜에 대해서는 대기를 신청할 수 없다")
    void 예약_대기_실패() {
        assertThatThrownBy(() -> reservationWaitingFactory.create("현미밥", pastReservation))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("예약 대기 삭제한다.")
    void 예약_대기_삭제() {
        ReservationWaiting saved = jdbcReservationWaitingRepository.save(
                reservationWaitingFactory.create("현미밥", futureReservation));
        jdbcReservationWaitingRepository.deleteById(saved.getId());
        assertThat(jdbcReservationWaitingRepository.findByName("현미밥")).hasSize(0);
    }

    @Test
    @DisplayName("이름으로 예약 대기 목록을 조회한다.")
    void 예약_대기_findByName() {
        jdbcReservationWaitingRepository.save(reservationWaitingFactory.create("현미밥", futureReservation));
        jdbcReservationWaitingRepository.save(reservationWaitingFactory.create("현미밥", futureReservation));

        List<ReservationWaiting> result = jdbcReservationWaitingRepository.findByName("현미밥");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("현미밥");
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2099, 12, 1));
    }

    @Test
    @DisplayName("대기 순번을 계산한다.")
    void 예약_대기_calculateTurn() {
        jdbcReservationWaitingRepository.save(reservationWaitingFactory.create("현미밥1", futureReservation));
        ReservationWaiting waiting2 = jdbcReservationWaitingRepository.save(
                reservationWaitingFactory.create("현미밥2", futureReservation));
        jdbcReservationWaitingRepository.save(reservationWaitingFactory.create("현미밥3", futureReservation));

        Map<Long, Long> turns = jdbcReservationWaitingRepository.calculateTurn("현미밥2");
        assertThat(turns.get(waiting2.getId())).isEqualTo(2L);
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
