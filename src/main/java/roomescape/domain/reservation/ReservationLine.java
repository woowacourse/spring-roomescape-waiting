package roomescape.domain.reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.exception.DuplicateException;

public class ReservationLine {

    private final ReservationSlot slot;
    private final Reservations reservations;

    public ReservationLine(ReservationSlot slot, List<Reservation> reservations) {
        validateSlot(slot);
        validateReservations(reservations);
        this.slot = slot;
        validateSameSlot(reservations);
        this.reservations = new Reservations(reservations);
    }

    public Reservation add(String name, LocalDateTime now) {
        validateDuplicated(name);
        return reservations.add(name, slot, now);
    }

    public Reservation move(Reservation reservation, LocalDateTime now) {
        validateDuplicated(reservation.getName());
        return reservation.updateSlot(slot, now, reservations.decideReservationStatus());
    }

    public boolean validateAndCheckSameSlot(Reservation reservation, ReservationSlot updateSlot, LocalDateTime now) {
        validateReservationInLine(reservation);
        reservation.validateUpdatable(updateSlot, now);
        return isSameSlot(updateSlot);
    }

    public Optional<Reservation> findPromotedReservationAfterCancel(Reservation target, LocalDateTime now) {
        validateReservationInLine(target);
        target.validateCancelable(now);

        return findPromotedReservation(target);
    }

    public Optional<Reservation> findNextToPromote(Reservation target) {
        return findPromotedReservation(target);
    }

    public Optional<Integer> findWaitingIndex(Reservation target) {
        validateSameSlotTarget(target);

        return reservations.findWaitingIndex(target);
    }

    private Optional<Reservation> findPromotedReservation(Reservation target) {
        if (target.isWaiting()) {
            return Optional.empty();
        }
        return findPromotedFirstWaiting();
    }

    private void validateDuplicated(String name) {
        if (reservations.hasSameReservationOwner(name) || reservations.hasSameWaitingOwner(name)) {
            throw new DuplicateException("이미 예약 또는 대기 중인 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
        }
    }

    private boolean containsReservation(Reservation target) {
        return reservations.containsReservation(target);
    }

    private boolean isSameSlot(ReservationSlot otherSlot) {
        return slot.isSameSlot(otherSlot);
    }

    private Optional<Reservation> findPromotedFirstWaiting() {
        return reservations.findPromotedFirstWaiting();
    }

    private void validateSlot(ReservationSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("예약 슬롯은 필수입니다.");
        }
    }

    private void validateReservations(List<Reservation> reservations) {
        if (reservations == null) {
            throw new IllegalArgumentException("예약 목록은 필수입니다.");
        }
    }

    private void validateSameSlot(List<Reservation> reservations) {
        boolean hasDifferentSlot = reservations.stream()
                .anyMatch(target -> !target.getSlot().isSameSlot(slot));

        if (hasDifferentSlot) {
            throw new IllegalArgumentException("대기 위치는 같은 예약 슬롯에 대해서만 계산 가능합니다.");
        }
    }

    private void validateReservationInLine(Reservation target) {
        validateSameSlotTarget(target);
        if (!containsReservation(target)) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다.");
        }
    }

    private void validateSameSlotTarget(Reservation target) {
        if (target == null) {
            throw new IllegalArgumentException("예약은 필수입니다.");
        }
        if (!target.getSlot().isSameSlot(slot)) {
            throw new IllegalArgumentException("같은 예약 슬롯의 예약만 처리할 수 있습니다.");
        }
    }
}
