package roomescape.service.reservationtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.BookingStatus;
import roomescape.domain.ReservationTime;
import roomescape.service.dto.request.ReservationTimeSaveRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("존재하지 않는 예약 시간인 경우 성공한다")
    void checkDuplicateTime_Success() {
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(LocalTime.of(13, 0));

        assertThatCode(() -> reservationTimeService.createReservationTime(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 존재하는 예약 시간인 경우 예외가 발생한다.")
    void checkDuplicateTime_Failure() {
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(LocalTime.of(11, 0));

        assertThatThrownBy(() -> reservationTimeService.createReservationTime(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 예약 시간입니다.");
    }

    @Test
    @DisplayName("날짜와 테마가 주어지면 각 시간의 예약 여부를 구한다.")
    void findAvailabilityByDateAndTheme() {
        LocalDate date = LocalDate.now().plusDays(1L);
        BookingStatus bookingStatus = reservationTimeService.findTimeSlotsBookingStatus(date, 1L);
        assertThat(bookingStatus.getReservationStatus())
                .isEqualTo(Map.of(
                        new ReservationTime(1L, LocalTime.of(10, 0)), true,
                        new ReservationTime(2L, LocalTime.of(11, 0)), false,
                        new ReservationTime(3L, LocalTime.of(12, 0)), false)
                );
    }

    @Test
    @DisplayName("예약 중이 아닌 시간을 삭제할 시 성공한다.")
    void deleteNotReservedTime_Success() {
        assertThatCode(() -> reservationTimeService.deleteReservationTime(3L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 예약 중인 시간을 삭제할 시 예외가 발생한다.")
    void deleteReservedTime_Failure() {
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 예약중인 시간은 삭제할 수 없습니다.");
    }
}
