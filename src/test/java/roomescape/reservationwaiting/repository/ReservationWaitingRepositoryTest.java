package roomescape.reservationwaiting.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
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
    private ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationWaitingFactory reservationWaitingFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Reservation pastReservation;
    private Reservation futureReservation1;
    private Reservation futureReservation2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마A', '설명A', 'https://a.com')");

        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', ?, 1, 1)",
                LocalDate.now().minusDays(1));
        Long pastReservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
        pastReservation = reservationRepository.findById(pastReservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', '2099-12-01', 1, 1)");
        Long futureReservationId1 = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
        futureReservation1 = reservationRepository.findById(futureReservationId1)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', '2099-12-02', 1, 1)");
        Long futureReservationId2 = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
        futureReservation2 = reservationRepository.findById(futureReservationId2)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @Test
    @DisplayName("예약 대기 신청에 성공한다.")
    void 예약_대기_성공() {
        ReservationWaiting saved = reservationWaitingRepository.save(
                reservationWaitingFactory.create("현미밥", futureReservation1));
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
        ReservationWaiting saved = reservationWaitingRepository.save(
                reservationWaitingFactory.create("현미밥", futureReservation1));
        reservationWaitingRepository.deleteById(saved.getId());
        assertThat(reservationWaitingRepository.findByName("현미밥")).hasSize(0);
    }

    @Test
    @DisplayName("이름으로 예약 대기 목록을 조회한다.")
    void 예약_대기_findByName() {
        reservationWaitingRepository.save(reservationWaitingFactory.create("현미밥", futureReservation1));
        reservationWaitingRepository.save(reservationWaitingFactory.create("현미밥", futureReservation2));

        List<ReservationWaiting> result = reservationWaitingRepository.findByName("현미밥");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("현미밥");
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2099, 12, 1));
    }

    @Test
    @DisplayName("대기 순번을 계산한다.")
    void 예약_대기_calculateTurn() {
        reservationWaitingRepository.save(reservationWaitingFactory.create("현미밥1", futureReservation1));
        ReservationWaiting waiting2 = reservationWaitingRepository.save(
                reservationWaitingFactory.create("현미밥2", futureReservation1));
        reservationWaitingRepository.save(reservationWaitingFactory.create("현미밥3", futureReservation1));

        Map<Long, Long> turns = reservationWaitingRepository.calculateTurn("현미밥2");
        assertThat(turns.get(waiting2.getId())).isEqualTo(2L);
    }

    @Test
    @DisplayName("date, themeId, timdId로 대기 객체를 받아온다.")
    void 예약_대기_조회() {
        reservationWaitingRepository.save(reservationWaitingFactory.create("현미밥1", futureReservation1));
        Optional<ReservationWaiting> waiting = reservationWaitingRepository.findOldestBySlot(
                futureReservation1.getSlot());
        assertThat(waiting).isPresent();
        assertThat(waiting.get().getName()).isEqualTo("현미밥1");
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
