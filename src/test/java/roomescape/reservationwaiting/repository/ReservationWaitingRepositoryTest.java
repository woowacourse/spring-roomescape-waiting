package roomescape.reservationwaiting.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.domain.ReservationWaitingFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationWaitingRepositoryTest {

    @Autowired
    private JdbcReservationWaitingRepository jdbcReservationWaitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationWaitingFactory reservationWaitingFactory;

    @Test
    @DisplayName("예약 대기 신청에 성공한다.")
    void 예약_대기_성공() {
        Reservation reservation = reservationRepository.findById(12L).orElseThrow(() -> new BusinessException(
                ErrorCode.RESERVATION_NOT_FOUND));
        ReservationWaiting saved = jdbcReservationWaitingRepository.save(
                reservationWaitingFactory.create("현미밥", reservation)
        );
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("이미 지난 시간과 날짜에 대해서는 대기를 신청할 수 없다")
    void 예약_대기_실패() {
        Reservation reservation = reservationRepository.findById(1L).orElseThrow(() -> new BusinessException(
                ErrorCode.RESERVATION_NOT_FOUND));
        assertThatThrownBy(() -> reservationWaitingFactory.create("현미밥", reservation))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("예약 대기 삭제한다.")
    void 예약_대기_삭제() {
        Reservation reservation = reservationRepository.findById(12L).orElseThrow(() -> new BusinessException(
                ErrorCode.RESERVATION_NOT_FOUND));
        ReservationWaiting saved = jdbcReservationWaitingRepository.save(
                reservationWaitingFactory.create("현미밥", reservation)
        );
        Long id = saved.getId();
        jdbcReservationWaitingRepository.deleteById(id);
        assertThat(jdbcReservationWaitingRepository.findByName("현미밥")).hasSize(0);
    }

    @Test
    @DisplayName("이름으로 예약 대기 상황을 조회한다.")
    void 예약_대기_조회() {
        Reservation reservation = reservationRepository.findById(12L).orElseThrow(() -> new BusinessException(
                ErrorCode.RESERVATION_NOT_FOUND));
        jdbcReservationWaitingRepository.save(reservationWaitingFactory.create("현미밥1", reservation));
        jdbcReservationWaitingRepository.save(reservationWaitingFactory.create("현미밥2", reservation));
        jdbcReservationWaitingRepository.save(reservationWaitingFactory.create("현미밥3", reservation));
        List<Long> turns = jdbcReservationWaitingRepository.calculateTurn("현미밥2");
        assertThat(turns.getFirst()).isEqualTo(3);
    }
}
