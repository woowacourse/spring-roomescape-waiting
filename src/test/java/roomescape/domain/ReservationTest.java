package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationStatus.PendingStatus;

class ReservationTest {

    private static final Long THEME_SLOT_ID = 1L;
    private static final Theme THEME = new Theme(1L, "테마", "설명", "test.com");
    private static final LocalDate DATE = LocalDate.now().plusDays(1);
    private static final Time TIME = new Time(1L, LocalTime.of(10, 0));

    @Test
    @DisplayName("정상적인 값을 입력하면 예약 객체가 생성된다.")
    void create_ValidParameters_CreatesReservation() {
        Reservation reservation = new Reservation(1L, "브라운", THEME_SLOT_ID, DATE, TIME, THEME, PendingStatus.getInstance());
        assertThat(reservation.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("예약자 이름이 null이거나 비어있으면 예외가 발생한다.")
    void create_InvalidName_ThrowsException() {
        assertThatThrownBy(() -> new Reservation(1L, " ", THEME_SLOT_ID, DATE, TIME, THEME, PendingStatus.getInstance()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약 시간 객체가 null이면 예외가 발생한다.")
    void create_NullTime_ThrowsException() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", THEME_SLOT_ID, DATE, null, THEME, PendingStatus.getInstance()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("transientOf를 통해 비영속 상태의 예약 객체를 생성할 수 있다.")
    void transientOf_ValidParameters_CreatesTransientReservation() {
        Reservation reservation = new Reservation("브라운", THEME_SLOT_ID, DATE, TIME, THEME);
        assertThat(reservation.getId()).isNull();
    }
}
