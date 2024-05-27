package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class BookStatusTest {

    @ParameterizedTest
    @EnumSource(value = BookStatus.class, names = "BOOKED", mode = Mode.EXCLUDE)
    @DisplayName("확정되지 않은 예약을 취소할 수 없다.")
    void cancelOnNotBookedReservation(BookStatus status) {
        assertThatCode(status::cancelBooking)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("확정된 예약이 아닙니다.");
    }

    @ParameterizedTest
    @EnumSource(value = BookStatus.class, names = "WAITING", mode = Mode.EXCLUDE)
    @DisplayName("예약 대기중이 아닌 예약을 대기취소할 수 없다.")
    void cancelOnNotWaitingReservation(BookStatus status) {
        assertThatCode(status::cancelWaiting)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대기 중인 예약이 아닙니다.");
    }

    @ParameterizedTest
    @EnumSource(value = BookStatus.class, names = "WAITING", mode = Mode.EXCLUDE)
    @DisplayName("대기 중이 아닌 예약을 확정할 수 없다.")
    void bookOnNotWaitingReservation(BookStatus status) {
        assertThatCode(status::book)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대기 중인 예약이 아닙니다.");
    }

    @Test
    @DisplayName("대기 중인 예약을 확정할 수 있다.")
    void bookOnWaitingReservation() {
        BookStatus status = BookStatus.WAITING;
        assertThat(status.book()).isEqualTo(BookStatus.BOOKED);
    }

    @Test
    @DisplayName("대기 중인 예약을 취소할 수 있다.")
    void cancelWaitingOnWaitingReservation() {
        BookStatus status = BookStatus.WAITING;
        assertThat(status.cancelWaiting()).isEqualTo(BookStatus.WAITING_CANCELLED);
    }

    @Test
    @DisplayName("확정된 예약을 취소할 수 있다.")
    void cancelBookingOnBookedReservation() {
        BookStatus status = BookStatus.BOOKED;
        assertThat(status.cancelBooking()).isEqualTo(BookStatus.BOOKING_CANCELLED);
    }
}
