package roomescape.reservation.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.exception.ReservationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static roomescape.reservation.domain.ReservationStatus.*;
import static roomescape.reservation.exception.ReservationErrorInformation.*;

class ReservationsTest {

    private final Long slotId = 1L;
    private final String name = "송송";
    private final String anotherName = "다른사람";

    @Test
    @DisplayName("슬롯 예약 목록에 요청자의 예약이 있으면 예외가 발생한다.")
    void validateNotAlreadyBookedBy_fail() {
        // given
        Reservations reservations = new Reservations(List.of(
                Reservation.load(1L, name, slotId, RESERVED, LocalDateTime.now())
        ));

        // when & then
        Assertions.assertThatThrownBy(() -> reservations.validateNotAlreadyBookedBy(name))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_ALREADY_BOOKED.getMessage());
    }

    @Test
    @DisplayName("슬롯 예약 목록에 요청자의 예약이 없으면 예외가 발생하지 않는다.")
    void validateNotAlreadyBookedBy_success() {
        // given
        Reservations reservations = new Reservations(List.of(
                Reservation.load(1L, anotherName, slotId, RESERVED, LocalDateTime.now())
        ));

        // when & then
        Assertions.assertThatCode(() -> reservations.validateNotAlreadyBookedBy(name))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("확정 예약이 있으면 true를 반환한다.")
    void hasReserved_returnTrue() {
        // given
        Reservations reservations = new Reservations(List.of(
                Reservation.load(1L, anotherName, slotId, RESERVED, LocalDateTime.now())
        ));

        // when & then
        Assertions.assertThat(reservations.hasReserved())
                .isTrue();
    }

    @Test
    @DisplayName("결제 대기중인 예약이 있어도 true를 반환한다.")
    void hasReserved_returnTrue_when_pending_payment() {
        // given
        Reservations reservations = new Reservations(List.of(
                Reservation.load(1L, anotherName, slotId, PENDING_PAYMENT, LocalDateTime.now())
        ));

        // when & then
        Assertions.assertThat(reservations.hasReserved())
                .isTrue();
    }

    @Test
    @DisplayName("확정 예약이 없으면 false를 반환한다.")
    void hasReserved_returnFalse() {
        // given
        Reservations reservations = new Reservations(List.of());

        // when & then
        Assertions.assertThat(reservations.hasReserved())
                .isFalse();
    }

    @Test
    @DisplayName("빈 슬롯에 예약하면 결제 대기 상태로 추가된다.")
    void reserve_adds_new_reservation() {
        // given
        Reservations reservations = new Reservations(new ArrayList<>());

        // when
        Reservation result = reservations.reserve(name, slotId, LocalDateTime.now());

        // then
        Assertions.assertThat(result.getStatus())
                .isEqualTo(PENDING_PAYMENT);

        Assertions.assertThat(reservations.values())
                .hasSize(1);
    }

    @Test
    @DisplayName("이미 예약된 슬롯에 다른 사람이 예약하면 WAITING 상태로 추가된다.")
    void reserve_adds_waiting_when_slot_taken() {
        // given
        Reservations reservations = new Reservations(List.of(
                Reservation.load(1L, name, slotId, RESERVED, LocalDateTime.now())
        ));

        // when
        Reservation result = reservations.reserve(anotherName, slotId, LocalDateTime.now());

        // then
        Assertions.assertThat(result.getStatus())
                .isEqualTo(WAITING);
    }

    @Test
    @DisplayName("결제 대기중인 슬롯에 다른 사람이 예약하면 WAITING 상태로 추가된다.")
    void reserve_adds_waiting_when_pending_payment_slot_taken() {
        // given
        Reservations reservations = new Reservations(List.of(
                Reservation.load(1L, name, slotId, PENDING_PAYMENT, LocalDateTime.now())
        ));

        // when
        Reservation result = reservations.reserve(anotherName, slotId, LocalDateTime.now());

        // then
        Assertions.assertThat(result.getStatus())
                .isEqualTo(WAITING);
    }

    @Test
    @DisplayName("예약을 취소하면 CANCELED 상태가 되고, 다음 대기자가 승격된다.")
    void cancel_reserved_promotes_waiting() {
        // given
        Reservation reserved = Reservation.load(1L, name, slotId, RESERVED, LocalDateTime.now());
        Reservation waiting = Reservation.load(2L, anotherName, slotId, WAITING, LocalDateTime.now().plusSeconds(1));
        Reservations reservations = new Reservations(List.of(reserved, waiting));

        // when
        Reservations changed = reservations.cancel(reserved.getId(), reserved.getName());
        Reservation canceled = changed.findById(reserved.getId());
        Reservation promoted = changed.findById(waiting.getId());

        // then
        Assertions.assertThat(changed.values()).hasSize(2);

        Assertions.assertThat(canceled.getStatus())
                .isEqualTo(CANCELED);

        Assertions.assertThat(promoted.getStatus())
                .isEqualTo(PENDING_PAYMENT);
    }

    @Test
    @DisplayName("결제 대기중인 예약을 취소하면 CANCELED 상태가 되고, 다음 대기자가 승격된다.")
    void cancel_pending_payment_promotes_waiting() {
        // given
        Reservation pendingPayment = Reservation.load(1L, name, slotId, PENDING_PAYMENT, LocalDateTime.now());
        Reservation waiting = Reservation.load(2L, anotherName, slotId, WAITING, LocalDateTime.now().plusSeconds(1));
        Reservations reservations = new Reservations(List.of(pendingPayment, waiting));

        // when
        Reservations changed = reservations.cancel(pendingPayment.getId(), pendingPayment.getName());
        Reservation canceled = changed.findById(pendingPayment.getId());
        Reservation promoted = changed.findById(waiting.getId());

        // then
        Assertions.assertThat(changed.values()).hasSize(2);

        Assertions.assertThat(canceled.getStatus())
                .isEqualTo(CANCELED);

        Assertions.assertThat(promoted.getStatus())
                .isEqualTo(PENDING_PAYMENT);
    }

    @Test
    @DisplayName("대기 예약을 취소하면 CANCELED 상태만 되고 승격 없다.")
    void cancel_waiting_no_promotion() {
        // given
        Reservation waiting = Reservation.load(1L, name, slotId, WAITING, LocalDateTime.now());
        Reservations reservations = new Reservations(List.of(waiting));

        // when
        Reservations changed = reservations.cancel(waiting.getId(), waiting.getName());
        Reservation canceled = changed.values().get(0);

        // then
        Assertions.assertThat(changed.values())
                .hasSize(1);

        Assertions.assertThat(canceled.getStatus())
                .isEqualTo(CANCELED);
    }

    @Test
    @DisplayName("관리자 취소 시 소유권 검증 없이 취소하고 대기자를 승격한다.")
    void cancelByManager_promotes_waiting() {
        // given
        Reservation reserved = Reservation.load(1L, name, slotId, RESERVED, LocalDateTime.now());
        Reservation waiting = Reservation.load(2L, anotherName, slotId, WAITING, LocalDateTime.now().plusSeconds(1));
        Reservations reservations = new Reservations(List.of(reserved, waiting));

        // when
        Reservations changed = reservations.cancelByManager(reserved.getId());
        Reservation canceled = changed.findById(reserved.getId());
        Reservation promoted = changed.findById(waiting.getId());

        // then
        Assertions.assertThat(changed.values()).hasSize(2);

        Assertions.assertThat(canceled.getStatus())
                .isEqualTo(CANCELED);

        Assertions.assertThat(promoted.getStatus())
                .isEqualTo(PENDING_PAYMENT);
    }

    @Test
    @DisplayName("예약+대기 목록에서 승격자는 가장 빨리 예약요청을 보낸 자이다.")
    void promoteWaiting_by_reservedAt() {
        // given
        Reservation firstWait = Reservation.load(1L, "test1", slotId, WAITING, LocalDateTime.now());
        Reservation secondWait = Reservation.load(2L, "test2", slotId, WAITING, LocalDateTime.now().plusDays(1));
        Reservations reservations = new Reservations(List.of(firstWait, secondWait));

        // when
        Reservation promoted = reservations.promoteWaiting().get();

        // then
        Assertions.assertThat(promoted)
                .isEqualTo(firstWait);
    }

    @Test
    @DisplayName("예약요청 시각이 같으면 ID가 낮은 대기자가 승격된다.")
    void promoteWaiting_same_reservedAt_by_id() {
        // given
        LocalDateTime sameTime = LocalDateTime.now();
        Reservation later = Reservation.load(2L, "test2", slotId, WAITING, sameTime);
        Reservation earlier = Reservation.load(1L, "test1", slotId, WAITING, sameTime);
        Reservations reservations = new Reservations(List.of(later, earlier));

        // when
        Reservation promoted = reservations.promoteWaiting().get();

        // then
        Assertions.assertThat(promoted)
                .isEqualTo(earlier);
    }

    @Test
    @DisplayName("이미 예약이 존재하면 WAITING 상태를 반환한다.")
    void decideStatus() {
        // given
        Reservations reservations = new Reservations(List.of(
                Reservation.load(1L, anotherName, slotId, RESERVED, LocalDateTime.now())
        ));

        // when
        ReservationStatus actual = reservations.decideStatus(name);

        // then
        Assertions.assertThat(actual)
                .isEqualTo(WAITING);
    }

    @Test
    @DisplayName("예약이 없으면 결제대기 상태를 반환한다.")
    void decideStatus_empty() {
        // given
        Reservations reservations = new Reservations(List.of(
        ));

        // when
        ReservationStatus actual = reservations.decideStatus(name);

        // then
        Assertions.assertThat(actual)
                .isEqualTo(PENDING_PAYMENT);
    }

    @Test
    @DisplayName("예약을 변경할때, 기존 예약에 대기가 있으면 승격한다.")
    void reschedule() {
        // given
        Reservation reserved = Reservation.load(1L, name, slotId, RESERVED, LocalDateTime.now());
        Reservation waiting = Reservation.load(2L, anotherName, slotId, WAITING, LocalDateTime.now().plusSeconds(1));
        Reservations reservations = new Reservations(List.of(reserved, waiting));
        Long newSlotId = 2L;

        // when
        Reservations changed = reservations.reschedule(newSlotId, reserved.getId(), reserved.getName(), RESERVED);
        Reservation rescheduled = changed.findById(reserved.getId());
        Reservation promoted = changed.findById(waiting.getId());

        // then
        Assertions.assertThat(changed.values()).hasSize(2);

        Assertions.assertThat(rescheduled.getStatus())
                .isEqualTo(RESERVED);

        Assertions.assertThat(promoted.getStatus())
                .isEqualTo(PENDING_PAYMENT);
    }

}
