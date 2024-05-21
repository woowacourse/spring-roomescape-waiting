package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.BasicAcceptanceTest;
import roomescape.exception.RoomescapeException;

class ReservationServiceTest extends BasicAcceptanceTest {
    @Autowired
    private ReservationService reservationService;

    @DisplayName("예약 삭제 요청시 예약이 존재하지 않으면 예외를 반환한다.")
    @Test
    void shouldThrowsIllegalArgumentExceptionWhenReservationDoesNotExist() {
        assertThatCode(() -> reservationService.deleteById(99L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("httpStatus")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
