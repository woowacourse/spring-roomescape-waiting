package roomescape.domain;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.ReservationStatus;

class ReservationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 7, 12, 0);
    private static final User USER = new User("u@test.com", Password.ofEncrypted("pw"), "브라운", Role.MEMBER)
            .withId(1L);
    private static final Theme THEME = new Theme(1L, "테마", "설명", "https://thumbnail.url");
    private static final ReservationTime TIME = new ReservationTime(1L, LocalTime.of(12, 0));
    private static final Store STORE = new Store(1L, "매장");
    private static final LocalDate DATE = LocalDate.of(2026, 5, 8);

    @Test
    @DisplayName("예약자가 null이면 예외")
    void throwsExceptionWhenUserIsNull() {
        assertThatThrownBy(() -> new Reservation(null, null, THEME, DATE, TIME, STORE, ReservationStatus.RESERVED))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }

    @Test
    @DisplayName("테마가 null이면 예외")
    void throwsExceptionWhenThemeIsNull() {
        assertThatThrownBy(() -> new Reservation(null, USER, null, DATE, TIME, STORE, ReservationStatus.RESERVED))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }

    @Test
    @DisplayName("예약 날짜가 null이면 예외")
    void throwsExceptionWhenDateIsNull() {
        assertThatThrownBy(() -> new Reservation(null, USER, THEME, null, TIME, STORE, ReservationStatus.RESERVED))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }

    @Test
    @DisplayName("예약 시간이 null이면 예외")
    void throwsExceptionWhenTimeIsNull() {
        assertThatThrownBy(() -> new Reservation(null, USER, THEME, DATE, null, STORE, ReservationStatus.RESERVED))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }

    @Test
    @DisplayName("매장이 null이면 예외")
    void throwsExceptionWhenStoreIsNull() {
        assertThatThrownBy(() -> new Reservation(null, USER, THEME, DATE, TIME, null, ReservationStatus.RESERVED))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }

    @Test
    @DisplayName("상태가 null이면 예외")
    void throwsExceptionWhenStatusIsNull() {
        assertThatThrownBy(() -> new Reservation(null, USER, THEME, DATE, TIME, STORE, null))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }

    @Test
    @DisplayName("isInPast - 과거 날짜면 true")
    void isInPastReturnsTrueForPastDate() {
        Reservation reservation = build(LocalDate.of(2026, 5, 6), LocalTime.of(12, 0));
        assertThat(reservation.isInPast(NOW)).isTrue();
    }

    @Test
    @DisplayName("isInPast - 미래 날짜면 false")
    void isInPastReturnsFalseForFutureDate() {
        Reservation reservation = build(LocalDate.of(2026, 5, 8), LocalTime.of(12, 0));
        assertThat(reservation.isInPast(NOW)).isFalse();
    }

    @Test
    @DisplayName("isInPast - 당일 1분 전이면 true")
    void isInPastReturnsTrueWhenOneMinuteBeforeOnSameDay() {
        Reservation reservation = build(LocalDate.of(2026, 5, 7), LocalTime.of(11, 59));
        assertThat(reservation.isInPast(NOW)).isTrue();
    }

    @Test
    @DisplayName("isInPast - 당일 1분 후면 false")
    void isInPastReturnsFalseWhenOneMinuteAfterOnSameDay() {
        Reservation reservation = build(LocalDate.of(2026, 5, 7), LocalTime.of(12, 1));
        assertThat(reservation.isInPast(NOW)).isFalse();
    }

    @Test
    @DisplayName("isInPast - 현재와 정확히 같은 시간이면 false")
    void isInPastReturnsFalseWhenExactlyNow() {
        Reservation reservation = build(LocalDate.of(2026, 5, 7), LocalTime.of(12, 0));
        assertThat(reservation.isInPast(NOW)).isFalse();
    }

    private Reservation build(LocalDate date, LocalTime time) {
        Theme theme = new Theme(1L, "테마", "설명", "https://thumbnail.url");
        ReservationTime reservationTime = new ReservationTime(1L, time);
        return new Reservation(null, USER, theme, date, reservationTime, STORE, ReservationStatus.RESERVED);
    }
}
