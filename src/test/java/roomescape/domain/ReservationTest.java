package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationStatus.PendingStatus;

class ReservationTest {

    @Test
    @DisplayName("정상적인 값을 입력하면 예약 객체가 생성된다.")
    void create_ValidParameters_CreatesReservation() {
        Time time = new Time(1L, LocalTime.of(10, 0));
        ThemeSlot themeSlot = new ThemeSlot(1L, new Theme(1L, null, null, null), LocalDate.now().plusDays(1), time, false);
        Reservation reservation = new Reservation(1L, "브라운", themeSlot, PendingStatus.getInstance());
        assertThat(reservation.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("예약자 이름이 null이거나 비어있으면 예외가 발생한다.")
    void create_InvalidName_ThrowsException() {
        Time time = new Time(1L, LocalTime.of(10, 0));
        ThemeSlot themeSlot = new ThemeSlot(1L, new Theme(1L, null, null, null), LocalDate.now().plusDays(1), time, false);
        assertThatThrownBy(() -> new Reservation(1L, " ", themeSlot, PendingStatus.getInstance()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약 시간 객체가 null이면 예외가 발생한다.")
    void create_NullTime_ThrowsException() {
        ThemeSlot themeSlot = new ThemeSlot(1L, new Theme(1L, null, null, null), LocalDate.now().plusDays(1), null, false);
        assertThatThrownBy(() -> new Reservation(1L, "브라운", themeSlot, PendingStatus.getInstance()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("transientOf를 통해 비영속 상태의 예약 객체를 생성할 수 있다.")
    void transientOf_ValidParameters_CreatesTransientReservation() {
        ThemeSlot themeSlot = new ThemeSlot(1L, new Theme(1L, null, null, null), LocalDate.now().plusDays(1),
                new Time(1L, LocalTime.of(10, 0)), false);
        Reservation reservation = new Reservation("브라운", themeSlot);
        assertThat(reservation.getId()).isNull();
    }

    @Test
    @DisplayName("다른 예약 슬롯 식별자인지 판단한다.")
    void hasDifferentThemeSlot() {
        ThemeSlot themeSlot = new ThemeSlot(1L, new Theme(1L, null, null, null), LocalDate.now().plusDays(1),
                new Time(1L, LocalTime.of(10, 0)), false);
        Reservation reservation = new Reservation("브라운", themeSlot);

        assertThat(reservation.hasDifferentThemeSlot(2L)).isTrue();
        assertThat(reservation.hasDifferentThemeSlot(1L)).isFalse();
    }

}
