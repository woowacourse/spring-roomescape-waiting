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
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;
import roomescape.exception.ExpiredDateTimeException;

public class ReservationTest {

    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "무서운 이야기", "공포", "example.com");

    private static Slot slotOf(LocalDateTime dateTime) {
        return Slot.restore(1L, dateTime.toLocalDate(), new ReservationTime(1L, dateTime.toLocalTime()), theme);
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 0", "0, 1", "1, 1"})
    void 미래_슬롯으로_예약을_생성하면_정상_작동한다(int day, int hour) {
        Slot futureSlot = slotOf(LocalDateTime.now().plusDays(day).plusHours(hour));

        assertThatCode(() -> Reservation.create("브라운", futureSlot))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 0", "0, 1", "1, 1"})
    void 과거_슬롯으로_예약을_생성하면_예외가_발생한다(int day, int hour) {
        Slot pastSlot = slotOf(LocalDateTime.now().minusDays(day).minusHours(hour));

        assertThatThrownBy(() -> Reservation.create("브라운", pastSlot))
                .isExactlyInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void restore는_과거_슬롯이어도_예외_없이_복원된다() {
        Slot pastSlot = Slot.restore(1L, LocalDate.now().minusDays(1), reservationTime, theme);

        assertThatCode(() -> Reservation.restore(1L, pastSlot, "브라운", LocalDateTime.now()))
                .doesNotThrowAnyException();
    }

    @Test
    void restore로_생성된_예약의_필드가_올바르게_설정된다() {
        LocalDate date = LocalDate.now().plusDays(1);
        Slot slot = Slot.restore(1L, date, reservationTime, theme);
        LocalDateTime createdAt = LocalDateTime.now();

        Reservation reservation = Reservation.restore(1L, slot, "브라운", createdAt);

        assertThat(reservation.getId()).isEqualTo(1L);
        assertThat(reservation.getName()).isEqualTo("브라운");
        assertThat(reservation.getDate()).isEqualTo(date);
        assertThat(reservation.getTime()).isEqualTo(reservationTime);
        assertThat(reservation.getTheme()).isEqualTo(theme);
        assertThat(reservation.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void withName은_같은_슬롯에서_이름만_변경한다() {
        Slot slot = Slot.restore(1L, LocalDate.now().plusDays(1), reservationTime, theme);
        Reservation reservation = Reservation.restore(1L, slot, "브라운", LocalDateTime.now());

        Reservation renamed = reservation.update("네오");

        assertThat(renamed.getName()).isEqualTo("네오");
        assertThat(renamed.getId()).isEqualTo(1L);
        assertThat(renamed.getDate()).isEqualTo(slot.getDate());
        assertThat(renamed.getTime()).isEqualTo(slot.getTime());
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 0", "0, 1", "1, 1"})
    void 과거_슬롯의_예약은_withName_시_예외가_발생한다(int day, int hour) {
        Slot pastSlot = slotOf(LocalDateTime.now().minusDays(day).minusHours(hour));
        Reservation reservation = Reservation.restore(1L, pastSlot, "브라운", LocalDateTime.now());

        assertThatThrownBy(() -> reservation.update("네오"))
                .isExactlyInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void isReservedBy는_예약자_이름과_일치하는지_확인한다() {
        Slot slot = Slot.restore(1L, LocalDate.now().plusDays(1), reservationTime, theme);
        Reservation reservation = Reservation.restore(1L, slot, "브라운", LocalDateTime.now());

        assertThat(reservation.isReservedBy("브라운")).isTrue();
        assertThat(reservation.isReservedBy("네오")).isFalse();
    }
}
