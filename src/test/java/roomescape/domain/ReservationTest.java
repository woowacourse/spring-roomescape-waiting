package roomescape.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.DomainValidationException;

class ReservationTest {

    private static final ReservationTime VALID_TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final LocalDate VALID_DATE = LocalDate.of(2026, 1, 1);
    private static final Theme VALID_THEME = new Theme(
            1L,
            "무인도 탈출",
            "갯벌이 많은 무인도를 탈출하는 흥미진진 대탈출!",
            "https://picsum.photos/seed/roomescape1/800/600.jpg"
    );

    @Test
    @DisplayName("이름이 30자를 초과하면 예외가 발생한다")
    void 이름이_30자를_초과하면_예외가_발생한다() {
        String name = "밥".repeat(31);
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new Reservation(1L, name, VALID_DATE, VALID_TIME, VALID_THEME)
        );
        assertEquals("예약자 이름은 30자를 초과할 수 없습니다.", exception.getMessage());
    }
}
