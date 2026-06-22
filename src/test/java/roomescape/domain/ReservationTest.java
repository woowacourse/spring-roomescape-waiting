package roomescape.domain;

import org.junit.jupiter.api.Test;
import roomescape.domain.exception.DomainConflictException;
import roomescape.domain.exception.DomainRuleViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

class ReservationTest {

    private static final ReservationTime TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "공포방", "무서운방입니다.", "image-url");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 1, 0, 0);

    @Test
    void 유효한_값으로_예약을_생성하면_필드가_저장된다() {
        Reservation reservation = new Reservation(1L, "브라운", LocalDate.of(2026, 5, 1), TIME, THEME,
                ReservationStatus.CONFIRMED, null, null);

        assertThat(reservation.getName()).isEqualTo("브라운");
        assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(reservation.getTime()).isEqualTo(TIME);
    }

    @Test
    void 예약자_이름이_빈_문자열이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Reservation(1L, "", LocalDate.of(2026, 5, 1), TIME, THEME,
                ReservationStatus.CONFIRMED, null, null))
                .isInstanceOf(DomainRuleViolationException.class);
    }

    @Test
    void 예약자_이름이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Reservation(1L, null, LocalDate.of(2026, 5, 1), TIME, THEME,
                ReservationStatus.CONFIRMED, null, null))
                .isInstanceOf(DomainRuleViolationException.class);
    }

    @Test
    void 예약_날짜가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", null, TIME, THEME,
                ReservationStatus.CONFIRMED, null, null))
                .isInstanceOf(DomainRuleViolationException.class);
    }

    @Test
    void 예약_시간이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", LocalDate.of(2026, 5, 1), null, THEME,
                ReservationStatus.CONFIRMED, null, null))
                .isInstanceOf(DomainRuleViolationException.class);
    }

    @Test
    void 예약_테마가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", LocalDate.of(2026, 5, 1), TIME, null,
                ReservationStatus.CONFIRMED, null, null))
                .isInstanceOf(DomainRuleViolationException.class);
    }

    @Test
    void 미래_시간으로_예약을_생성할_수_있다() {
        assertThatNoException().isThrownBy(
                () -> Reservation.create("브라운", LocalDate.of(2026, 5, 10), TIME, THEME, NOW, "order-1", 50000L));
    }

    @Test
    void 과거_시간으로_예약을_생성하면_도메인_충돌_예외가_발생한다() {
        assertThatThrownBy(
                () -> Reservation.create("브라운", LocalDate.of(2026, 4, 1), TIME, THEME, NOW, "order-1", 50000L))
                .isInstanceOf(DomainConflictException.class);
    }

    @Test
    void 본인의_미래_예약은_일정을_변경할_수_있다() {
        Reservation reservation = new Reservation(
                7L, "브라운", LocalDate.of(2026, 5, 10), TIME, THEME, ReservationStatus.CONFIRMED, null, null);
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(12, 0));

        Reservation updated = reservation.changeSchedule(
                LocalDate.of(2026, 5, 11), newTime, "브라운", NOW);

        assertThat(updated.getId()).isEqualTo(7L);
        assertThat(updated.getName()).isEqualTo("브라운");
        assertThat(updated.getDate()).isEqualTo(LocalDate.of(2026, 5, 11));
        assertThat(updated.getTime()).isEqualTo(newTime);
        assertThat(updated.getTheme()).isEqualTo(THEME);
    }

    @Test
    void 본인의_예약이_아니면_변경할_수_없다() {
        Reservation reservation = new Reservation(
                7L, "브라운", LocalDate.of(2026, 5, 10), TIME, THEME, ReservationStatus.CONFIRMED, null, null);
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(12, 0));

        assertThatThrownBy(() -> reservation.changeSchedule(
                LocalDate.of(2026, 5, 11), newTime, "어셔", NOW))
                .isInstanceOf(DomainConflictException.class);
    }

    @Test
    void 지난_예약은_변경할_수_없다() {
        Reservation reservation = new Reservation(
                7L, "브라운", LocalDate.of(2026, 4, 10), TIME, THEME, ReservationStatus.CONFIRMED, null, null);
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(12, 0));

        assertThatThrownBy(() -> reservation.changeSchedule(
                LocalDate.of(2026, 5, 11), newTime, "브라운", NOW))
                .isInstanceOf(DomainConflictException.class);
    }

    @Test
    void 과거_시간으로_변경할_수_없다() {
        Reservation reservation = new Reservation(
                7L, "브라운", LocalDate.of(2026, 5, 10), TIME, THEME, ReservationStatus.CONFIRMED, null, null);
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(12, 0));

        assertThatThrownBy(() -> reservation.changeSchedule(
                LocalDate.of(2026, 4, 11), newTime, "브라운", NOW))
                .isInstanceOf(DomainConflictException.class);
    }

    @Test
    void 같은_id를_가진_예약은_같은_예약이다() {
        Reservation reservation = new Reservation(
                7L, "브라운", LocalDate.of(2026, 5, 10), TIME, THEME, ReservationStatus.CONFIRMED, null, null);
        Reservation same = new Reservation(
                7L, "브라운", LocalDate.of(2026, 5, 10), TIME, THEME, ReservationStatus.CONFIRMED, null, null);

        assertThat(reservation.isSameReservation(same)).isTrue();
    }

    @Test
    void 다른_id를_가진_예약은_같은_예약이_아니다() {
        Reservation reservation = new Reservation(
                7L, "브라운", LocalDate.of(2026, 5, 10), TIME, THEME, ReservationStatus.CONFIRMED, null, null);
        Reservation other = new Reservation(
                8L, "어셔", LocalDate.of(2026, 5, 10), TIME, THEME, ReservationStatus.CONFIRMED, null, null);

        assertThat(reservation.isSameReservation(other)).isFalse();
    }

    @Test
    void 본인의_미래_예약은_취소할_수_있다() {
        Reservation reservation = new Reservation(
                7L, "브라운", LocalDate.of(2026, 5, 10), TIME, THEME, ReservationStatus.CONFIRMED, null, null);

        assertThatNoException().isThrownBy(() -> reservation.checkCancellable("브라운", NOW));
    }

    @Test
    void 본인의_예약이_아니면_취소할_수_없다() {
        Reservation reservation = new Reservation(
                7L, "브라운", LocalDate.of(2026, 5, 10), TIME, THEME, ReservationStatus.CONFIRMED, null, null);

        assertThatThrownBy(() -> reservation.checkCancellable("어셔", NOW))
                .isInstanceOf(DomainConflictException.class);
    }

    @Test
    void 지난_예약은_취소할_수_없다() {
        Reservation reservation = new Reservation(
                7L, "브라운", LocalDate.of(2026, 4, 10), TIME, THEME, ReservationStatus.CONFIRMED, null, null);

        assertThatThrownBy(() -> reservation.checkCancellable("브라운", NOW))
                .isInstanceOf(DomainConflictException.class);
    }
}
