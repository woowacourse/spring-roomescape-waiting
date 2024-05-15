package roomescape.service.reservationtime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.service.dto.request.ReservationTimeSaveRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationTimeCreateServiceTest {

    @Autowired
    private ReservationTimeCreateService reservationTimeCreateService;

    @Test
    @DisplayName("존재하지 않는 예약 시간인 경우 성공한다")
    void checkDuplicateTime_Success() {
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(LocalTime.of(12, 0));

        assertThatCode(() -> reservationTimeCreateService.createReservationTime(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 존재하는 예약 시간인 경우 예외가 발생한다.")
    void checkDuplicateTime_Failure() {
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(LocalTime.of(11, 0));

        assertThatThrownBy(() -> reservationTimeCreateService.createReservationTime(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 예약 시간입니다.");
    }
}
