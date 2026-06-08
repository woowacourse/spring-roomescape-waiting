package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.InvalidRequestException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = new Theme(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
    private final LocalDate date = LocalDate.of(2026, 5, 15);
    private final LocalDateTime beforeReservation = LocalDateTime.of(2026, 5, 15, 9, 0);

    @Test
    @DisplayName("예약자 이름이 비어있으면 도메인 예외가 발생한다.")
    void create_fail_whenNameIsBlank() {
        assertInvalidRequestException(
                () -> Reservation.create(" ", date, time, theme, beforeReservation)
        );
    }

    @Test
    @DisplayName("예약 id가 null이면 도메인 예외가 발생한다.")
    void withId_fail_whenIdIsNull() {
        Reservation reservation = reservation(null);

        assertInvalidRequestException(
                () -> reservation.withId(null)
        );
    }

    @Test
    @DisplayName("이미 id가 있는 예약에 id를 부여하면 도메인 예외가 발생한다.")
    void withId_fail_whenReservationAlreadyHasId() {
        Reservation reservation = reservation(1L);

        assertInvalidRequestException(
                () -> reservation.withId(2L)
        );
    }

    @Test
    @DisplayName("과거 날짜와 시간으로 예약을 생성하면 예외가 발생한다.")
    void create_fail_whenReservationDateTimeIsBeforeNow() {
        LocalDate date = LocalDate.of(2026, 5, 15);
        LocalDateTime now = LocalDateTime.of(2026, 5, 15, 11, 0);

        assertInvalidRequestException(
                () -> Reservation.create("브라운", date, time, theme, now)
        );
    }

    @Test
    @DisplayName("예약 날짜와 시간이 기준 시각보다 이후이면 예약을 생성한다.")
    void create_success_whenReservationDateTimeIsAfterNow() {
        Reservation reservation = Reservation.create("브라운", date, time, theme, beforeReservation);

        assertThat(reservation.getSlot()).isEqualTo(new Slot(date, time, theme));
    }

    @Test
    @DisplayName("저장된 예약은 과거 날짜와 시간이어도 재구성한다.")
    void reconstruct_success_whenReservationDateTimeIsBeforeNow() {
        Slot pastSlot = new Slot(LocalDate.of(2026, 5, 15), time, theme);

        Reservation reservation = Reservation.reconstruct(1L, "브라운", pastSlot);

        assertThat(reservation.getId()).isEqualTo(1L);
        assertThat(reservation.getSlot()).isEqualTo(pastSlot);
    }

    @Test
    @DisplayName("저장된 예약의 이름이 비어있으면 재구성할 수 없다.")
    void reconstruct_fail_whenNameIsBlank() {
        assertInvalidRequestException(
                () -> Reservation.reconstruct(1L, " ", new Slot(date, time, theme))
        );
    }

    @Test
    @DisplayName("저장된 예약의 슬롯이 비어있으면 재구성할 수 없다.")
    void reconstruct_fail_whenSlotIsNull() {
        assertInvalidRequestException(
                () -> Reservation.reconstruct(1L, "브라운", null)
        );
    }

    @Test
    @DisplayName("예약 날짜와 시간이 기준 시각보다 이전이면 과거 예약이다.")
    void isPastAt_success_whenReservationDateTimeIsBeforeNow() {
        Reservation reservation = reservation(null);
        LocalDateTime now = LocalDateTime.of(2026, 5, 15, 11, 0);

        assertThat(reservation.isPast(now)).isTrue();
    }

    @Test
    @DisplayName("예약 날짜와 시간이 기준 시각과 같으면 과거 예약이 아니다.")
    void isPastAt_false_whenReservationDateTimeIsSameAsNow() {
        Reservation reservation = reservation(null);
        LocalDateTime now = LocalDateTime.of(2026, 5, 15, 10, 0);

        assertThat(reservation.isPast(now)).isFalse();
    }

    @Test
    @DisplayName("예약 날짜와 시간이 기준 시각보다 이후이면 과거 예약이 아니다.")
    void isPastAt_false_whenReservationDateTimeIsAfterNow() {
        Reservation reservation = reservation(null);
        LocalDateTime now = LocalDateTime.of(2026, 5, 15, 9, 0);

        assertThat(reservation.isPast(now)).isFalse();
    }

    private void assertInvalidRequestException(Runnable runnable) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(InvalidRequestException.class);
    }

    private Reservation reservation(Long id) {
        return Reservation.reconstruct(id, "브라운", new Slot(date, time, theme));
    }
}
