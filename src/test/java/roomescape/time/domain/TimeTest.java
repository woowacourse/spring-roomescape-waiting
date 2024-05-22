package roomescape.time.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.exception.BadRequestException;

class TimeTest {
    @Test
    @DisplayName("시간이 null일 경우 예외가 발생한다.")
    void validation_ShouldThrowException_WhenStartAtIsNull() {
        assertAll(() -> {
                    Throwable nullStartAt = assertThrows(
                            BadRequestException.class, () -> new Time(null));
                    assertEquals("시간 값이 정의되지 않은 요청입니다.", nullStartAt.getMessage());
                }
        );
    }

    @Test
    @DisplayName("운영 시간보다 빠르거나, 끝나는 시간보다 늦을 경우 예외가 발생한다.")
    void validation_ShouldThrowException_WhenStartAtIsNotOpeningHour() {
        assertAll(() -> {
                    assertThatThrownBy(() -> new Time(LocalTime.of(7, 59)))
                            .isInstanceOf(BadRequestException.class)
                            .hasMessage("운영 시간 외의 예약 시간 요청입니다.");

                    assertThatThrownBy(() -> new Time(LocalTime.of(23, 1)))
                            .isInstanceOf(BadRequestException.class)
                            .hasMessage("운영 시간 외의 예약 시간 요청입니다.");
                }
        );
    }
}
