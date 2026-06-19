package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationStatus.CancelledStatus;
import roomescape.domain.reservationStatus.CompletedStatus;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.global.exception.CustomException;

class ReservationTest {

    private static final Long THEME_SLOT_ID = 1L;
    private static final Theme THEME = new Theme(1L, "테마", "설명", "test.com", 10000L);
    private static final LocalDate DATE = LocalDate.now().plusDays(1);
    private static final Time TIME = new Time(1L, LocalTime.of(10, 0));

    @Test
    @DisplayName("유효한 값으로 예약 객체를 생성할 수 있다.")
    void create_ValidParameters_CreatesReservation() {
        Reservation reservation = new Reservation(1L, "브라운", THEME_SLOT_ID, DATE, TIME, THEME, PendingStatus.getInstance());
        assertThat(reservation.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("비영속 예약 객체는 ID가 null이다.")
    void transientOf_ValidParameters_CreatesTransientReservation() {
        Reservation reservation = new Reservation("브라운", THEME_SLOT_ID, DATE, TIME, THEME);
        assertThat(reservation.getId()).isNull();
    }

    @Test
    @DisplayName("예약자 이름이 빈 문자열이면 예외가 발생한다.")
    void create_BlankName_ThrowsException() {
        assertThatThrownBy(() -> new Reservation(1L, " ", THEME_SLOT_ID, DATE, TIME, THEME, PendingStatus.getInstance()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약 날짜가 null이면 예외가 발생한다.")
    void create_NullDate_ThrowsException() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", THEME_SLOT_ID, null, TIME, THEME, PendingStatus.getInstance()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약 시간이 null이면 예외가 발생한다.")
    void create_NullTime_ThrowsException() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", THEME_SLOT_ID, DATE, null, THEME, PendingStatus.getInstance()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("테마가 null이면 예외가 발생한다.")
    void create_NullTheme_ThrowsException() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", THEME_SLOT_ID, DATE, TIME, null, PendingStatus.getInstance()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("초기 생성된 예약은 PENDING 상태이다.")
    void newReservation_IsPending() {
        Reservation reservation = new Reservation("브라운", THEME_SLOT_ID, DATE, TIME, THEME);
        assertThat(reservation.isPending()).isTrue();
        assertThat(reservation.isConfirmed()).isFalse();
        assertThat(reservation.isCancelled()).isFalse();
    }

    @Test
    @DisplayName("confirm() 호출 후 CONFIRMED 상태가 된다.")
    void confirm_ChangesStatusToConfirmed() {
        Reservation reservation = new Reservation("브라운", THEME_SLOT_ID, DATE, TIME, THEME);
        reservation.confirm();
        assertThat(reservation.isConfirmed()).isTrue();
        assertThat(reservation.isPending()).isFalse();
    }

    @Test
    @DisplayName("PENDING 예약을 cancel() 하면 CANCELLED 상태가 된다.")
    void cancel_FromPending_ChangesStatusToCancelled() {
        Reservation reservation = new Reservation("브라운", THEME_SLOT_ID, DATE, TIME, THEME);
        reservation.cancel();
        assertThat(reservation.isCancelled()).isTrue();
    }

    @Test
    @DisplayName("CONFIRMED 예약을 cancel() 하면 CANCELLED 상태가 된다.")
    void cancel_FromConfirmed_ChangesStatusToCancelled() {
        Reservation reservation = new Reservation("브라운", THEME_SLOT_ID, DATE, TIME, THEME);
        reservation.confirm();
        reservation.cancel();
        assertThat(reservation.isCancelled()).isTrue();
    }

    @Test
    @DisplayName("CONFIRMED 예약을 complete() 하면 COMPLETED 상태가 된다.")
    void complete_FromConfirmed_ChangesStatusToCompleted() {
        Reservation reservation = new Reservation("브라운", THEME_SLOT_ID, DATE, TIME, THEME);
        reservation.confirm();
        reservation.complete();
        assertThat(reservation.getReservationStatus()).isEqualTo(CompletedStatus.getInstance());
    }

    @Test
    @DisplayName("이미 취소된 예약을 다시 취소하면 예외가 발생한다.")
    void cancel_AlreadyCancelled_ThrowsException() {
        Reservation reservation = new Reservation("브라운", THEME_SLOT_ID, DATE, TIME, THEME);
        reservation.cancel();
        assertThatThrownBy(reservation::cancel)
                .isInstanceOf(CustomException.class)
                .hasMessage("취소할 수 없는 예약입니다.");
    }

    @Test
    @DisplayName("완료된 예약을 취소하면 예외가 발생한다.")
    void cancel_Completed_ThrowsException() {
        Reservation reservation = new Reservation("브라운", THEME_SLOT_ID, DATE, TIME, THEME);
        reservation.confirm();
        reservation.complete();
        assertThatThrownBy(reservation::cancel)
                .isInstanceOf(CustomException.class)
                .hasMessage("취소할 수 없는 예약입니다.");
    }
}
