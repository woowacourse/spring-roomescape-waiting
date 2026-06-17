package roomescape.slot.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.date.domain.ReservationDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Reservations;
import roomescape.reservation.exception.ReservationException;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservation.domain.ReservationStatus.*;
import static roomescape.reservation.exception.ReservationErrorInformation.*;

class ReservationSlotTest {

    private final LocalDate futureDate = LocalDate.now().plusMonths(1);
    private final LocalDate pastDate = LocalDate.now().minusDays(1);

    private final ReservationDate reservationDate = ReservationDate.create(futureDate);
    private final ReservationDate pastReservationDate = ReservationDate.load(99L, pastDate, true);
    private final ReservationTime time1 = ReservationTime.create(LocalTime.of(15, 40));
    private final ReservationTime time2 = ReservationTime.create(LocalTime.of(16, 40));
    private final Theme theme = ThemeFixture.activeTheme();

    private final ReservationSlot futureSlot = ReservationSlot.load(1L, reservationDate, time1, theme);
    private final ReservationSlot pastSlot = ReservationSlot.load(2L, pastReservationDate, time1, theme);
    private final ReservationSlot slot = ReservationSlot.load(3L, reservationDate, time2, theme);

    @Test
    @DisplayName("정상적인 테마/날짜/시간으로 슬롯을 생성할 수 있다.")
    void of() {
        // when & then
        assertThatCode(() -> ReservationSlot.of(reservationDate, time1, theme))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 시간이 null이면 예외가 발생한다.")
    void validateTime() {
        // given
        ReservationTime nullTime = null;

        // when & then
        assertThatThrownBy(() -> ReservationSlot.of(reservationDate, nullTime, theme))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_TIME_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("예약 날짜가 null이면 예외가 발생한다.")
    void validateDate() {
        // given
        ReservationDate nullDate = null;

        // when & then
        assertThatThrownBy(() -> ReservationSlot.of(nullDate, time1, theme))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_DATE_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("테마가 null이면 예외가 발생한다.")
    void validateTheme() {
        // given
        Theme nullTheme = null;

        // when & then
        assertThatThrownBy(() -> ReservationSlot.of(reservationDate, time1, nullTheme))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_THEME_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("미래 슬롯에 예약하면 결제 대기 상태로 성공한다.")
    void reserve_future_slot() {
        // given
        ReservationSlot slot = futureSlot.withReservations(new Reservations(new ArrayList<>()));

        // when
        Reservation result = slot.reserve("한다");

        // then
        assertThat(result.getStatus()).isEqualTo(PENDING_PAYMENT);
        assertThat(result.getSlotId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이미 지난 슬롯에 예약하면 예외가 발생한다.")
    void reserve_past_slot() {
        // given
        ReservationSlot slot = pastSlot.withReservations(new Reservations(new ArrayList<>()));

        // when & then
        assertThatThrownBy(() -> slot.reserve("한다"))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
    }

    @Test
    @DisplayName("슬롯의 예약을 취소하면, 취소 대상은 CANCEL, 대기 1순위는 RESERVED가 된다.")
    void cancel() {
        // given
        String name = "송송";
        String waiterName = "대기자";
        Reservation reserved = Reservation.load(1L, name, 2L, RESERVED, LocalDateTime.now());
        Reservation waiting = Reservation.load(2L, waiterName, 2L, WAITING, LocalDateTime.now());
        ReservationSlot mySlot = slot.withReservations(new Reservations(List.of(reserved, waiting)));

        // when
        Reservations changed = mySlot.cancel(reserved.getId(), reserved.getName());
        Reservation canceled = changed.findById(reserved.getId());
        Reservation promoted = changed.findById(waiting.getId());

        // then
        Assertions.assertThat(canceled.getStatus())
                .isEqualTo(CANCELED);

        Assertions.assertThat(promoted.getStatus())
                .isEqualTo(PENDING_PAYMENT);
    }

    @Test
    @DisplayName("이미 지난 슬롯의 예약을 취소하면 예외가 발생한다.")
    void cancel_past_slot() {
        // given
        Reservation reserved = Reservation.load(1L, "한다", 2L, RESERVED, LocalDateTime.now());
        ReservationSlot slot = pastSlot.withReservations(new Reservations(List.of(reserved)));

        // when & then
        assertThatThrownBy(() -> slot.cancel(reserved.getId(), reserved.getName()))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
    }

    @Test
    @DisplayName("관리자가 이미 지난 슬롯의 예약을 취소하면 예외가 발생한다.")
    void cancelByManager_past_slot() {
        // given
        Reservation reserved = Reservation.load(1L, "한다", 2L, RESERVED, LocalDateTime.now());
        ReservationSlot slot = pastSlot.withReservations(new Reservations(List.of(reserved)));

        // when & then
        assertThatThrownBy(() -> slot.cancelByManager(reserved.getId()))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
    }

    @Test
    @DisplayName("이미 지난 슬롯의 예약을 변경하면 예외가 발생한다.")
    void reschedule_from_past_slot() {
        // given
        Reservation reserved = Reservation.load(1L, "한다", 2L, RESERVED, LocalDateTime.now());
        ReservationSlot slot = pastSlot.withReservations(new Reservations(List.of(reserved)));

        // when & then
        assertThatThrownBy(() -> slot.reschedule(futureSlot, reserved.getId(), reserved.getName()))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
    }

    @Test
    @DisplayName("관리자가 이미 지난 슬롯의 예약을 변경하면 예외가 발생한다.")
    void rescheduleByManager_from_past_slot() {
        // given
        Reservation reserved = Reservation.load(1L, "한다", 2L, RESERVED, LocalDateTime.now());
        ReservationSlot slot = pastSlot.withReservations(new Reservations(List.of(reserved)));

        // when & then
        assertThatThrownBy(() -> slot.rescheduleByManager(futureSlot, reserved.getId()))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
    }

    @Test
    @DisplayName("이미 지난 슬롯의 예약으로 변경하면 예외가 발생한다.")
    void reschedule_to_past_slot() {
        // given
        Reservation reserved = Reservation.load(1L, "한다", 2L, RESERVED, LocalDateTime.now());
        ReservationSlot slot = futureSlot.withReservations(new Reservations(List.of(reserved)));

        // when & then
        assertThatThrownBy(() -> slot.reschedule(pastSlot, reserved.getId(), reserved.getName()))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
    }

    @Test
    @DisplayName("관리자가 이미 지난 슬롯의 예약을 변경하면 예외가 발생한다.")
    void rescheduleByManager_to_past_slot() {
        // given
        Reservation reserved = Reservation.load(1L, "한다", 2L, RESERVED, LocalDateTime.now());
        ReservationSlot slot = futureSlot.withReservations(new Reservations(List.of(reserved)));

        // when & then
        assertThatThrownBy(() -> slot.rescheduleByManager(pastSlot, reserved.getId()))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_PAST.getMessage());
    }

    @Test
    @DisplayName("변경할 슬롯에 예약이 있으면 대기로 변경된다.")
    void reschedule_to_waiting() {
        // given
        String requesterName = "송송";
        Reservation reserved = Reservation.load(1L, requesterName, 2L, RESERVED, LocalDateTime.now());
        Reservation waiting = Reservation.load(2L, "대기중인 사람", 2L, WAITING, LocalDateTime.now());
        Reservations myReservations = new Reservations(List.of(reserved, waiting));

        ReservationSlot mySlot = slot.withReservations(myReservations);

        Reservations changeTargetReservations = new Reservations(List.of(
                Reservation.load(3L, "다른사람", 2L, RESERVED, LocalDateTime.now())
        ));
        ReservationSlot alreadyReservedSlot = futureSlot.withReservations(changeTargetReservations);

        // when
        Reservations changed = mySlot.reschedule(alreadyReservedSlot, reserved.getId(), reserved.getName());
        Reservation actual = changed.findById(reserved.getId());

        // then
        Assertions.assertThat(actual.getStatus())
                .isEqualTo(WAITING);
    }

    @Test
    @DisplayName("변경시 기존 슬롯에 대기가 있으면 승격된다.")
    void reschedule_promote() {
        // given
        String requesterName = "송송";
        String waitingName = "대기중인 사람";
        Reservation reserved = Reservation.load(1L, requesterName, 2L, RESERVED, LocalDateTime.now());
        Reservation waiting = Reservation.load(2L, waitingName, 2L, WAITING, LocalDateTime.now());
        Reservations myReservations = new Reservations(List.of(reserved, waiting));
        ReservationSlot mySlot = slot.withReservations(myReservations);

        Reservations changeTargetReservations = new Reservations(List.of(
                Reservation.load(3L, "다른사람", 2L, RESERVED, LocalDateTime.now())
        ));
        ReservationSlot alreadyReservedSlot = futureSlot.withReservations(changeTargetReservations);

        // when
        Reservations changed = mySlot.reschedule(alreadyReservedSlot, reserved.getId(), reserved.getName());
        Reservation actual = changed.findById(waiting.getId());

        // then
        Assertions.assertThat(actual.getStatus())
                .isEqualTo(PENDING_PAYMENT);
    }

}
