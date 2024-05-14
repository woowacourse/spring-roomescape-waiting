package roomescape.time.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.time.exception.TimeExceptionCode;

public class TimeTest {

    @Test
    @DisplayName("전달 받은 데이터로 Time 객체를 정상적으로 생성한다.")
    void constructTime() {
        Time time = new Time(1, LocalTime.of(9, 0));

        assertAll(
                () -> assertEquals(time.getId(), 1),
                () -> assertEquals(time.getStartAt(), LocalTime.of(9, 0))
        );
    }

    @Test
    @DisplayName("시간이 null일 경우 예외가 발생한다.")
    void validation_ShouldThrowException_WhenStartAtIsNull() {
        Throwable nullStartAt = assertThrows(
                RoomEscapeException.class, () -> new Time(null));
        assertEquals(TimeExceptionCode.FOUND_TIME_IS_NULL_EXCEPTION.getMessage(), nullStartAt.getMessage());
    }

    @Test
    @DisplayName("추가하는 시간이 운영 시간보다 빠를 경우 예외가 발생한다.")
    void validation_ShouldThrowException_WhenStartAtIsBeforeOpeningHour() {
        Throwable beforeOpenTime = assertThrows(
                RoomEscapeException.class, () -> new Time(LocalTime.of(7, 59)));
        assertEquals(TimeExceptionCode.TIME_IS_OUT_OF_OPERATING_TIME.getMessage(), beforeOpenTime.getMessage());
    }

    @Test
    @DisplayName("추가하려는 시간이 운영 시간보다 느릴 경우 예외가 발생한다.")
    void validation_ShouldThrowException_WhenStartAtIsAfterEndHour() {
        Throwable afterCloseTime = assertThrows(
                RoomEscapeException.class, () -> new Time(LocalTime.of(23, 1)));
        assertEquals(TimeExceptionCode.TIME_IS_OUT_OF_OPERATING_TIME.getMessage(), afterCloseTime.getMessage());
    }
}
