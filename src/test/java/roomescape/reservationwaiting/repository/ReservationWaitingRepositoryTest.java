package roomescape.reservationwaiting.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.domain.ReservationWaitingFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
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

    private Long pastReservationId;
    private Long futureReservationId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마A', '설명A', 'https://a.com')");

        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', ?, 1, 1)",
                LocalDate.now().minusDays(1));
        pastReservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);

        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', '2099-12-01', 1, 1)");
        futureReservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
    }

    @Test
    @DisplayName("예약 대기 신청에 성공한다.")
    void 예약_대기_성공() {
        Reservation reservation = reservationRepository.findById(futureReservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        ReservationWaiting saved = jdbcReservationWaitingRepository.save(
                reservationWaitingFactory.create("현미밥", reservation));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("이미 지난 시간과 날짜에 대해서는 대기를 신청할 수 없다")
    void 예약_대기_실패() {
        Reservation reservation = reservationRepository.findById(pastReservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        assertThatThrownBy(() -> reservationWaitingFactory.create("현미밥", reservation))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("예약 대기 삭제한다.")
    void 예약_대기_삭제() {
        Reservation reservation = reservationRepository.findById(futureReservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        ReservationWaiting saved = jdbcReservationWaitingRepository.save(
                reservationWaitingFactory.create("현미밥", reservation));
        jdbcReservationWaitingRepository.deleteById(saved.getId());
        assertThat(jdbcReservationWaitingRepository.findByName("현미밥")).hasSize(0);
    }

    @Test
    @DisplayName("이름으로 예약 대기 목록을 조회한다.")
    void 예약_대기_findByName() {
        Reservation reservation = reservationRepository.findById(futureReservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        jdbcReservationWaitingRepository.save(reservationWaitingFactory.create("현미밥", reservation));
        jdbcReservationWaitingRepository.save(reservationWaitingFactory.create("현미밥", reservation));

        List<ReservationWaiting> result = jdbcReservationWaitingRepository.findByName("현미밥");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("현미밥");
        assertThat(result.get(0).getReservation().getId()).isEqualTo(futureReservationId);
    }

    @Test
    @DisplayName("대기 순번을 계산한다.")
    void 예약_대기_calculateTurn() {
        Reservation reservation = reservationRepository.findById(futureReservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        jdbcReservationWaitingRepository.save(reservationWaitingFactory.create("현미밥1", reservation));
        ReservationWaiting waiting2 = jdbcReservationWaitingRepository.save(
                reservationWaitingFactory.create("현미밥2", reservation));
        jdbcReservationWaitingRepository.save(reservationWaitingFactory.create("현미밥3", reservation));

        Map<Long, Long> turns = jdbcReservationWaitingRepository.calculateTurn("현미밥2");
        assertThat(turns.get(waiting2.getId())).isEqualTo(2L);
    }
}
