package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class ReservationTest {

    private final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = Theme.of("우테코", "우테코 전용 테마", "https://example.com");

    @Test
    @DisplayName("성공적으로 예약 도메인 객체를 생성한다.")
    void of_validInput_returnsReservation() {
        // given
        String name = "브라운";
        LocalDate date = LocalDate.now().plusDays(1);

        // when
        Reservation reservation = Reservation.of(name, date, reservationTime, theme);

        // then
        assertThat(reservation.id()).isNull();
        assertThat(reservation.name()).isEqualTo(name);
        assertThat(reservation.date()).isEqualTo(date);
        assertThat(reservation.time()).isEqualTo(reservationTime);
        assertThat(reservation.theme()).isEqualTo(theme);
    }

    @Test
    @DisplayName("생성된 예약 객체의 필드 값을 확인한다.")
    void constructor_validInput_storesFields() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);

        Reservation reservation = new Reservation(1L, "제임스", date, reservationTime, theme);

        // then
        assertThat(reservation.id()).isEqualTo(1L);
        assertThat(reservation.name()).isEqualTo("제임스");
        assertThat(reservation.date()).isEqualTo(date);
        assertThat(reservation.time()).isEqualTo(reservationTime);
        assertThat(reservation.theme()).isEqualTo(theme);
    }

    @Test
    @DisplayName("예약 일자 및 시간을 성공적으로 수정한다.")
    void update_validInput_returnsUpdatedReservation() {
        // given
        Reservation original = new Reservation(1L, "브라운", LocalDate.now().plusDays(1), reservationTime, theme);
        LocalDate newDate = LocalDate.now().plusDays(2);
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(14, 0));

        // when
        Reservation updated = original.update(newDate, newTime);

        // then
        assertThat(updated.id()).isEqualTo(original.id());
        assertThat(updated.name()).isEqualTo(original.name());
        assertThat(updated.date()).isEqualTo(newDate);
        assertThat(updated.time()).isEqualTo(newTime);
        assertThat(updated.theme()).isEqualTo(original.theme());
    }

    @Test
    @DisplayName("예약 일자나 시간이 null로 들어오면 기존 값을 유지한다.")
    void update_nullInput_keepsOriginalValues() {
        // given
        Reservation original = new Reservation(1L, "브라운", LocalDate.now().plusDays(1), reservationTime, theme);

        // when
        Reservation updated = original.update(null, null);

        // then
        assertThat(updated.date()).isEqualTo(original.date());
        assertThat(updated.time()).isEqualTo(original.time());
    }

    @Test
    @DisplayName("예약 소유자 이름이 일치하면 예외가 발생하지 않는다.")
    void validateOwner_matchName_doesNotThrow() {
        // given
        Reservation reservation = new Reservation(1L, "브라운", LocalDate.now().plusDays(1), reservationTime, theme);

        // when & then
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> reservation.validateOwner("브라운"));
    }

    @Test
    @DisplayName("예약 소유자 이름이 일치하지 않으면 예외가 발생한다.")
    void validateOwner_mismatchName_throwsException() {
        // given
        Reservation reservation = new Reservation(1L, "브라운", LocalDate.now().plusDays(1), reservationTime, theme);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> reservation.validateOwner("코니"))
                .isInstanceOf(roomescape.global.exception.ForbiddenException.class)
                .hasMessage(ReservationErrorCode.AUTHORIZATION_FAIL.getMessage());
    }

    @Test
    @DisplayName("예약 일자가 과거면 예외가 발생한다.")
    void validateExpiry_pastDate_throwsException() {
        // given
        Reservation reservation = new Reservation(1L, "브라운", LocalDate.now().minusDays(1), reservationTime, theme);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(reservation::validateExpiry)
                .isInstanceOf(roomescape.global.exception.InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.INVALID_DATE.getMessage());
    }

    @Test
    @DisplayName("동일한 이름인지 확인한다.")
    void hasSameName_returnsTrueOrFalse() {
        // given
        Reservation reservation = new Reservation(1L, "브라운", LocalDate.now().plusDays(1), reservationTime, theme);

        // when & then
        assertThat(reservation.hasSameName("브라운")).isTrue();
        assertThat(reservation.hasSameName("코니")).isFalse();
    }
}
