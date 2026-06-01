package roomescape.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.DomainValidationException;

class ReservationTimeTest {

    @Test
    @DisplayName("시작시간이 유효하면 예약시간을 생성한다")
    void 시작시간이_유효하면_예약시간을_생성한다() {
        assertDoesNotThrow(() -> new ReservationTime(1L, LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("id가 null이어도 예약시간을 생성할 수 있다")
    void id가_null이어도_예약시간을_생성할_수_있다() {
        assertDoesNotThrow(() -> new ReservationTime(null, LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("시작시간이 null이면 예외가 발생한다")
    void 시작시간이_null이면_예외가_발생한다() {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new ReservationTime(1L, null)
        );
        assertEquals("예약 시간은 비어 있을 수 없습니다.", exception.getMessage());
    }
}
