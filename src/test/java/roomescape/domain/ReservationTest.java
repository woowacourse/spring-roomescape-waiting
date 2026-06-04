package roomescape.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.InvalidDomainException;
import roomescape.domain.policy.ReservationPolicy;
import roomescape.support.AlwaysAllowPolicy;

class ReservationTest {

    private static final ReservationTime VALID_TIME =
            ReservationTime.withId(1L, LocalTime.of(10, 0));
    private static final LocalDate VALID_DATE = LocalDate.of(2026, 1, 1);
    private static final Theme VALID_THEME =
            Theme.withId(1L, "무인도 탈출",
                    "갯벌이 많은 무인도를 탈출하는 흥미진진 대탈출!",
                    "https://picsum.photos/seed/roomescape1/800/600.jpg");

    private static final ReservationPolicy ALLOW_ALL = new AlwaysAllowPolicy();

    @Test
    @DisplayName("새 예약을 생성할 수 있다")
    void 새_예약을_생성할_수_있다() {
        assertDoesNotThrow(() ->
                Reservation.create("브라운", VALID_DATE, VALID_TIME, VALID_THEME, ALLOW_ALL));
    }

    @Test
    @DisplayName("DB에서 재구성할 수 있다")
    void DB에서_재구성할_수_있다() {
        assertDoesNotThrow(() ->
                Reservation.withId(1L, "브라운", VALID_DATE, VALID_TIME, VALID_THEME));
    }


    @Test
    @DisplayName("이름이 null이면 예외가 발생한다")
    void throwWhenNameIsNull() {
        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> Reservation.withId(1L, null, VALID_DATE, VALID_TIME, VALID_THEME)
        );
        assertEquals("예약자 이름은 비어 있을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("이름이 빈문자열이면 예외가 발생한다")
    void 이름이_빈문자열이면_예외가_발생한다() {
        assertThrows(
                InvalidDomainException.class,
                () -> Reservation.withId(1L, "", VALID_DATE, VALID_TIME, VALID_THEME)
        );
    }

    @Test
    @DisplayName("이름이 공백만으로 이루어져 있으면 예외가 발생한다")
    void 이름이_공백만으로_이루어져_있으면_예외가_발생한다() {
        assertThrows(
                InvalidDomainException.class,
                () -> Reservation.withId(1L, "   ", VALID_DATE, VALID_TIME, VALID_THEME)
        );
    }

    @Test
    @DisplayName("이름이 30자를 초과하면 예외가 발생한다")
    void 이름이_30자를_초과하면_예외가_발생한다() {
        String name = "밥".repeat(31);
        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> Reservation.withId(1L, name, VALID_DATE, VALID_TIME, VALID_THEME)
        );
        assertEquals("예약자 이름은 30자를 초과할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("날짜가 null이면 예외가 발생한다")
    void 날짜가_null이면_예외가_발생한다() {
        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> Reservation.withId(1L, "브라운", null, VALID_TIME, VALID_THEME)
        );
        assertEquals("날짜는 비어 있을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("시간이 null이면 예외가 발생한다")
    void 시간이_null이면_예외가_발생한다() {
        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> Reservation.withId(1L, "브라운", VALID_DATE, null, VALID_THEME)
        );
        assertEquals("시간은 비어 있을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("테마가 null이면 예외가 발생한다")
    void 테마가_null이면_예외가_발생한다() {
        InvalidDomainException exception = assertThrows(
                InvalidDomainException.class,
                () -> Reservation.withId(1L, "브라운", VALID_DATE, VALID_TIME, null)
        );
        assertEquals("테마는 비어 있을 수 없습니다.", exception.getMessage());
    }


}
