package roomescape.reservationtime.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.model.RoomEscapeException;
import roomescape.reservationtime.exception.ReservationTimeExceptionCode;

class ReservationTimeTest {

    @Test
    @DisplayName("전달 받은 데이터로 Time 객체를 정상적으로 생성한다.")
    void constructTime() {
        ReservationTime time = new ReservationTime(1, LocalTime.of(9, 0));

        assertAll(
                () -> assertEquals(1, time.getId()),
                () -> assertEquals(LocalTime.of(9, 0), time.getStartAt())
        );
    }

    @Test
    @DisplayName("시간이 null일 경우 예외가 발생한다.")
    void validation_ShouldThrowException_WhenStartAtIsNull() {
        Throwable nullStartAt = assertThrows(
                RoomEscapeException.class, () -> new ReservationTime(null));
        assertEquals(ReservationTimeExceptionCode.FOUND_TIME_IS_NULL_EXCEPTION.getMessage(), nullStartAt.getMessage());
    }

    @Test
    @DisplayName("추가하는 시간이 운영 시간보다 빠를 경우 예외가 발생한다.")
    void validation_ShouldThrowException_WhenStartAtIsBeforeOpeningHour() {
        Throwable beforeOpenTime = assertThrows(
                RoomEscapeException.class, () -> new ReservationTime(LocalTime.of(7, 59)));
        assertEquals(ReservationTimeExceptionCode.TIME_IS_OUT_OF_OPERATING_TIME.getMessage(), beforeOpenTime.getMessage());
    }

    @Test
    @DisplayName("추가하려는 시간이 운영 시간보다 느릴 경우 예외가 발생한다.")
    void validation_ShouldThrowException_WhenStartAtIsAfterEndHour() {
        Throwable afterCloseTime = assertThrows(
                RoomEscapeException.class, () -> new ReservationTime(LocalTime.of(23, 1)));
        assertEquals(ReservationTimeExceptionCode.TIME_IS_OUT_OF_OPERATING_TIME.getMessage(), afterCloseTime.getMessage());
    }
}
