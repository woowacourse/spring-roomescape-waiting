package roomescape.domain;

import org.junit.jupiter.api.Test;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.ErrorCode;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = new Theme(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

    @Test
    void 예약자_이름이_비어있으면_도메인_예외가_발생한다() {
        assertDomainException(
                () -> new Reservation(" ", LocalDate.of(2023, 8, 5), time, theme),
                ErrorCode.INVALID_RESERVATION_NAME
        );
    }

    @Test
    void 예약_날짜가_null이면_도메인_예외가_발생한다() {
        assertDomainException(
                () -> new Reservation("브라운", null, time, theme),
                ErrorCode.INVALID_RESERVATION_DATE
        );
    }

    @Test
    void 예약_시간이_null이면_도메인_예외가_발생한다() {
        assertDomainException(
                () -> new Reservation("브라운", LocalDate.of(2023, 8, 5), null, theme),
                ErrorCode.INVALID_RESERVATION_TIME
        );
    }

    @Test
    void 예약_테마가_null이면_도메인_예외가_발생한다() {
        assertDomainException(
                () -> new Reservation("브라운", LocalDate.of(2023, 8, 5), time, null),
                ErrorCode.INVALID_THEME
        );
    }

    @Test
    void 예약_id가_null이면_도메인_예외가_발생한다() {
        Reservation reservation = new Reservation("브라운", LocalDate.of(2023, 8, 5), time, theme);

        assertDomainException(
                () -> reservation.withId(null),
                ErrorCode.INVALID_RESERVATION_ID
        );
    }

    @Test
    void 이미_id가_있는_예약에_id를_부여하면_도메인_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, "브라운", LocalDate.of(2023, 8, 5), time, theme);

        assertDomainException(
                () -> reservation.withId(2L),
                ErrorCode.RESERVATION_ALREADY_HAS_ID
        );
    }

    private void assertDomainException(Runnable runnable, ErrorCode errorCode) {
        assertThatThrownBy(runnable::run)
                .isInstanceOfSatisfying(DomainException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode)
                )
                .hasMessage(errorCode.message());
    }
}
