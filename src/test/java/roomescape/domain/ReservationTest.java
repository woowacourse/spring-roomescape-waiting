package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ExpiredDateTimeException;

public class ReservationTest {

    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "무서운 이야기", "공포", "example.com");

    @ParameterizedTest
    @CsvSource(value = {"1, 0", "0, 1", "1, 1"})
    void 미래_날짜_시간으로_예약을_생성하면_정상_작동한다(int day, int hour) {
        LocalDate futureDate = LocalDate.now().plusDays(day);
        ReservationTime futureTime = new ReservationTime(1L, LocalTime.now().plusHours(hour));

        assertThatCode(() -> Reservation.create("브라운", futureDate, futureTime, theme))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 0", "0, 1", "1, 1"})
    void 과거_날짜_시간으로_예약을_생성하면_예외가_발생한다(int day, int hour) {
        LocalDate pastDate = LocalDate.now().minusDays(day);
        ReservationTime pastTime = new ReservationTime(1L, LocalTime.now().minusHours(hour));

        assertThatThrownBy(() -> Reservation.create("브라운", pastDate, pastTime, theme))
                .isExactlyInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void restore는_과거_날짜_시간이어도_예외_없이_복원된다() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime pastTime = new ReservationTime(1L, LocalTime.parse("10:00"));

        assertThatCode(() -> Reservation.restore(1L, "브라운", pastDate, pastTime, theme, LocalDateTime.now()))
                .doesNotThrowAnyException();
    }

    @Test
    void restore로_생성된_예약의_필드가_올바르게_설정된다() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime createdAt = LocalDateTime.now();

        Reservation reservation = Reservation.restore(1L, "브라운", date, reservationTime, theme, createdAt);

        assertThat(reservation.getId()).isEqualTo(1L);
        assertThat(reservation.getName()).isEqualTo("브라운");
        assertThat(reservation.getDate()).isEqualTo(date);
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 0", "0, 1", "1, 1"})
    void 미래_날짜_시간의_예약은_수정_가능하다(int day, int hour) {
        LocalDate futureDate = LocalDate.now().plusDays(day);
        ReservationTime futureTime = new ReservationTime(1L, LocalTime.now().plusHours(hour));
        Reservation reservation = Reservation.restore(1L, "브라운", futureDate, futureTime, theme, LocalDateTime.now());

        assertThatCode(() -> reservation.update("브라운", futureDate, futureTime, theme))
                .doesNotThrowAnyException();
    }

    @Test
    void update로_변경된_예약의_필드가_올바르게_설정된다() {
        LocalDate originalDate = LocalDate.now().plusDays(1);
        ReservationTime originalTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        Reservation reservation = Reservation.restore(1L, "브라운", originalDate, originalTime, theme, LocalDateTime.now());

        LocalDate newDate = LocalDate.now().plusDays(2);
        ReservationTime newTime = new ReservationTime(2L, LocalTime.now().plusHours(2));
        Theme newTheme = new Theme(2L, "새 테마", "설명", "url");
        Reservation updated = reservation.update("네오", newDate, newTime, newTheme);

        assertThat(updated.getName()).isEqualTo("네오");
        assertThat(updated.getDate()).isEqualTo(newDate);
        assertThat(updated.getTime()).isEqualTo(newTime);
        assertThat(updated.getTheme()).isEqualTo(newTheme);
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 0", "0, 1", "1, 1"})
    void 과거_날짜_시간의_예약은_수정시_예외가_발생한다(int day, int hour) {
        LocalDate pastDate = LocalDate.now().minusDays(day);
        ReservationTime pastTime = new ReservationTime(1L, LocalTime.now().minusHours(hour));
        Reservation reservation = Reservation.restore(1L, "브라운", pastDate, pastTime, theme, LocalDateTime.now());

        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime futureTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        assertThatThrownBy(() -> reservation.update("브라운", futureDate, futureTime, theme))
                .isExactlyInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 미래_예약을_과거_날짜로_변경시_예외가_발생한다() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime futureTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        Reservation reservation = Reservation.restore(1L, "브라운", futureDate, futureTime, theme, LocalDateTime.now());

        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime pastTime = new ReservationTime(1L, LocalTime.now().minusHours(1));
        assertThatThrownBy(() -> reservation.update("브라운", pastDate, pastTime, theme))
                .isExactlyInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void 예약자_이름이_일치하면_isReserved가_true를_반환한다() {
        Reservation reservation = Reservation.restore(1L, "브라운", LocalDate.now().plusDays(1), reservationTime, theme, LocalDateTime.now());

        assertThat(reservation.isReservedBy("브라운")).isTrue();
    }

    @Test
    void 예약자_이름이_다르면_isReserved가_false를_반환한다() {
        Reservation reservation = Reservation.restore(1L, "브라운", LocalDate.now().plusDays(1), reservationTime, theme, LocalDateTime.now());

        assertThat(reservation.isReservedBy("네오")).isFalse();
    }

    @Test
    void withReservationId로_id가_설정된_예약이_반환된다() {
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation = Reservation.create("브라운", date, reservationTime, theme);

        Reservation withId = reservation.withReservationId(99L);

        assertThat(withId.getId()).isEqualTo(99L);
        assertThat(withId.getName()).isEqualTo("브라운");
    }

    @Test
    void 미래_예약은_isExpired가_false를_반환한다() {
        Reservation reservation = Reservation.restore(1L, "브라운", LocalDate.now().plusDays(1), reservationTime, theme, LocalDateTime.now());

        assertThat(reservation.isExpired()).isFalse();
    }

    @Test
    void 과거_예약은_isExpired가_true를_반환한다() {
        ReservationTime pastTime = new ReservationTime(1L, LocalTime.parse("10:00"));
        Reservation reservation = Reservation.restore(1L, "브라운", LocalDate.now().minusDays(1), pastTime, theme, LocalDateTime.now());

        assertThat(reservation.isExpired()).isTrue();
    }

    @Test
    void transferTo는_같은_슬롯에_이름만_바뀐_예약을_반환한다() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime futureTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        Reservation original = Reservation.restore(1L, "브라운", futureDate, futureTime, theme, LocalDateTime.now());

        Reservation transferred = original.transferTo("네오");

        assertThat(transferred.getName()).isEqualTo("네오");
        assertThat(transferred.getDate()).isEqualTo(futureDate);
        assertThat(transferred.getTime()).isEqualTo(futureTime);
        assertThat(transferred.getTheme()).isEqualTo(theme);
        assertThat(transferred.getId()).isNull();
    }

    @Test
    void 과거_예약에_transferTo를_호출하면_예외가_발생한다() {
        ReservationTime pastTime = new ReservationTime(1L, LocalTime.now().minusHours(1));
        Reservation expiredReservation = Reservation.restore(1L, "브라운", LocalDate.now().minusDays(1), pastTime, theme, LocalDateTime.now());

        assertThatThrownBy(() -> expiredReservation.transferTo("네오"))
                .isInstanceOf(ExpiredDateTimeException.class);
    }
}
