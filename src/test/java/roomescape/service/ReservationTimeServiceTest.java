package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.response.ReservationTimeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Transactional
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @DisplayName("예약 시간 추가 테스트")
    @Test
    void createReservationTime() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 1));
        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(reservationTimeRequest);
        assertThat(reservationTime.startAt()).isEqualTo(LocalTime.of(10, 1));
    }

    @DisplayName("모든 예약 시간 조회 테스트")
    @Test
    void findAllReservationTimes() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 1));
        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(reservationTimeRequest);

        List<ReservationTimeResponse> reservationTimes = reservationTimeService.findAllReservationTimes();
        assertThat(reservationTimes).hasSize(1);
    }

    @DisplayName("예약 시간 삭제 테스트")
    @Test
    void deleteReservationTime() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 1));
        ReservationTimeResponse savedReservationTime = reservationTimeService.createReservationTime(
                reservationTimeRequest);

        reservationTimeService.deleteReservationTime(savedReservationTime.id());
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.findAllReservationTimes();
        assertThat(reservationTimes).isEmpty();
    }
}
