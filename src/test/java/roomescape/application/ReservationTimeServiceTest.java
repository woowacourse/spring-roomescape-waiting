package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.BasicAcceptanceTest;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.dto.ReservationTimeRequest;
import roomescape.exception.RoomescapeException;

class ReservationTimeServiceTest extends BasicAcceptanceTest {
    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("이미 존재하는 예약 시간을 생성 요청하면 예외가 발생한다.")
    @Test
    void shouldThrowsIllegalStateExceptionWhenCreateExistStartAtTime() {
        ReservationTime reservationTime = reservationTimeRepository.findAll().get(0);
        ReservationTimeRequest request = new ReservationTimeRequest(reservationTime.getStartAt());

        assertThatCode(() -> reservationTimeService.save(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("httpStatus")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @DisplayName("예약에 사용된 예약 시간을 삭제 요청하면, 예외가 발생한다.")
    @Test
    void shouldThrowsExceptionReservationWhenReservedInTime() {
        ReservationTime reservationTime = reservationTimeRepository.findAll().get(0);

        assertThatCode(() -> reservationTimeService.deleteById(reservationTime.getId()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("httpStatus")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @DisplayName("존재하지 않는 예약 시간을 삭제 요청하면, IllegalArgumentException 예외가 발생한다.")
    @Test
    void shouldThrowsIllegalArgumentExceptionWhenReservationTimeDoesNotExist() {
        assertThatCode(() -> reservationTimeService.deleteById(99L))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");
    }
}
