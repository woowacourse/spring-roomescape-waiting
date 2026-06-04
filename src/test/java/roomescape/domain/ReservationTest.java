package roomescape.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    private static final ReservationStatus VALID_STATUS = ReservationStatus.CONFIRMED;

    @Test
    @DisplayName("이름,날짜,시간이 모두 유효하면 예약을 생성한다")
    void 이름_날짜_시간이_모두_유효하면_예약을_생성한다() {
        assertDoesNotThrow(() -> new Reservation(1L, "브라운", VALID_DATE, VALID_TIME, VALID_THEME, VALID_STATUS));
    }

    @Test
    @DisplayName("id가 null이어도 예약을 생성할 수 있다")
    void id가_null이어도_예약을_생성할_수_있다() {
        assertDoesNotThrow(() -> new Reservation(null, "브라운", VALID_DATE, VALID_TIME, VALID_THEME, VALID_STATUS));
    }

    @Test
    @DisplayName("이름이 null이면 예외가 발생한다")
    void throwWhenNameIsNull() {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new Reservation(1L, null, VALID_DATE, VALID_TIME, VALID_THEME, VALID_STATUS)
        );
        assertEquals("예약자 이름은 비어 있을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("이름이 빈문자열이면 예외가 발생한다")
    void 이름이_빈문자열이면_예외가_발생한다() {
        assertThrows(
                DomainValidationException.class,
                () -> new Reservation(1L, "", VALID_DATE, VALID_TIME, VALID_THEME, VALID_STATUS)
        );
    }

    @Test
    @DisplayName("이름이 공백만으로 이루어져 있으면 예외가 발생한다")
    void 이름이_공백만으로_이루어져_있으면_예외가_발생한다() {
        assertThrows(
                DomainValidationException.class,
                () -> new Reservation(1L, "   ", VALID_DATE, VALID_TIME, VALID_THEME, VALID_STATUS)
        );
    }

    @Test
    @DisplayName("이름이 30자를 초과하면 예외가 발생한다")
    void 이름이_30자를_초과하면_예외가_발생한다() {
        String name = "밥".repeat(31);
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new Reservation(1L, name, VALID_DATE, VALID_TIME, VALID_THEME, VALID_STATUS)
        );
        assertEquals("예약자 이름은 30자를 초과할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("날짜가 null이면 예외가 발생한다")
    void 날짜가_null이면_예외가_발생한다() {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new Reservation(1L, "브라운", null, VALID_TIME, VALID_THEME, VALID_STATUS)
        );
        assertEquals("예약 날짜는 비어 있을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("시간이 null이면 예외가 발생한다")
    void 시간이_null이면_예외가_발생한다() {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new Reservation(1L, "브라운", VALID_DATE, null, VALID_THEME, VALID_STATUS)
        );
        assertEquals("예약 시간은 비어 있을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("테마가 null이면 예외가 발생한다")
    void 테마가_null이면_예외가_발생한다() {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new Reservation(1L, "브라운", VALID_DATE, VALID_TIME, null, VALID_STATUS)
        );
        assertEquals("예약 테마는 비어 있을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("상태가 null이면 예외가 발생한다")
    void 상태가_null이면_예외가_발생한다() {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new Reservation(1L, "브라운", VALID_DATE, VALID_TIME, VALID_THEME, null)
        );
        assertEquals("예약 상태는 비어 있을 수 없습니다.", exception.getMessage());
    }
}
