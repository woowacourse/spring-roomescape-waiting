package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.InvalidDomainException;
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
    void 예약자가_null이면_예외() {
        assertThatThrownBy(() -> new Reservation(null, null, THEME, DATE, TIME, STORE, ReservationStatus.RESERVED))
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("예약자는 필수입니다.");
    }

    @Test
    void 테마가_null이면_예외() {
        assertThatThrownBy(() -> new Reservation(null, USER, null, DATE, TIME, STORE, ReservationStatus.RESERVED))
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("테마는 필수입니다.");
    }

    @Test
    void 예약_날짜가_null이면_예외() {
        assertThatThrownBy(() -> new Reservation(null, USER, THEME, null, TIME, STORE, ReservationStatus.RESERVED))
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("예약 날짜는 필수입니다.");
    }

    @Test
    void 예약_시간이_null이면_예외() {
        assertThatThrownBy(() -> new Reservation(null, USER, THEME, DATE, null, STORE, ReservationStatus.RESERVED))
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("예약 시간은 필수입니다.");
    }

    @Test
    void 매장이_null이면_예외() {
        assertThatThrownBy(() -> new Reservation(null, USER, THEME, DATE, TIME, null, ReservationStatus.RESERVED))
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("매장은 필수입니다.");
    }

    @Test
    void 상태가_null이면_예외() {
        assertThatThrownBy(() -> new Reservation(null, USER, THEME, DATE, TIME, STORE, null))
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("예약 상태는 필수입니다.");
    }

    @Test
    void isInPast_과거_날짜면_true() {
        Reservation reservation = build(LocalDate.of(2026, 5, 6), LocalTime.of(12, 0));
        assertThat(reservation.isInPast(NOW)).isTrue();
    }

    @Test
    void isInPast_미래_날짜면_false() {
        Reservation reservation = build(LocalDate.of(2026, 5, 8), LocalTime.of(12, 0));
        assertThat(reservation.isInPast(NOW)).isFalse();
    }

    @Test
    void isInPast_당일_1분_전이면_true() {
        Reservation reservation = build(LocalDate.of(2026, 5, 7), LocalTime.of(11, 59));
        assertThat(reservation.isInPast(NOW)).isTrue();
    }

    @Test
    void isInPast_당일_1분_후면_false() {
        Reservation reservation = build(LocalDate.of(2026, 5, 7), LocalTime.of(12, 1));
        assertThat(reservation.isInPast(NOW)).isFalse();
    }

    @Test
    void isInPast_현재와_정확히_같은_시간이면_false() {
        Reservation reservation = build(LocalDate.of(2026, 5, 7), LocalTime.of(12, 0));
        assertThat(reservation.isInPast(NOW)).isFalse();
    }

    @Test
    void confirm_예약_확정인_경우_예외() {
        Reservation reservation = build(LocalDate.of(2026, 5, 7), LocalTime.of(12, 0), ReservationStatus.RESERVED);
        assertThatThrownBy(reservation::confirm)
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("예약 대기 상태만 확정할 수 있습니다.");
    }

    @Test
    void confirm_예약_대기인_경우_올바르게_예약_확정으로_변경() {
        Reservation reservation = build(LocalDate.of(2026, 5, 7), LocalTime.of(12, 0), ReservationStatus.WAITING);
        Reservation confirmed = reservation.confirm();

        assertThat(confirmed.isReserved()).isTrue();
        assertThat(reservation.isWaiting()).isTrue();

    }

    private Reservation build(LocalDate date, LocalTime time) {
        Theme theme = new Theme(1L, "테마", "설명", "https://thumbnail.url");
        ReservationTime reservationTime = new ReservationTime(1L, time);
        return new Reservation(null, USER, theme, date, reservationTime, STORE, ReservationStatus.RESERVED);
    }

    private Reservation build(LocalDate date, LocalTime time, ReservationStatus status) {
        Theme theme = new Theme(1L, "테마", "설명", "https://thumbnail.url");
        ReservationTime reservationTime = new ReservationTime(1L, time);
        return new Reservation(null, USER, theme, date, reservationTime, STORE, status);
    }
}
