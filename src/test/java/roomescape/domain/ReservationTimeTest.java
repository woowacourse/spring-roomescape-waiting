package roomescape.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.InvalidDomainException;

class ReservationTimeTest {

    @Test
    @DisplayName("시작시간이 유효하면 예약시간을 생성한다")
    void 시작시간이_유효하면_예약시간을_생성한다() {
        assertDoesNotThrow(() -> ReservationTime.create(LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("DB에서 재구성할 수 있다")
    void DB에서_재구성할_수_있다() {
        assertDoesNotThrow(() -> ReservationTime.withId(1L, LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("시작시간이 null이면 예외가 발생한다")
    void 시작시간이_null이면_예외가_발생한다() {
        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> ReservationTime.create(null)
        );
        assertEquals("예약 시간은 비어 있을 수 없습니다.", exception.getMessage());
    }
}
