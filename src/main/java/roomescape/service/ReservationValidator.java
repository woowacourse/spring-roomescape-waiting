package roomescape.service;

import org.springframework.stereotype.Component;
import roomescape.domain.Reservation;
import roomescape.domain.Reserver;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;

import java.time.LocalDateTime;

@Component
public class ReservationValidator {

    private final ReservationRepository reservationRepository;

    public ReservationValidator(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void validateCreatableByUser(Reservation reservation, LocalDateTime now) {
        validateNotPast(reservation, now);
        validateAvailableSlot(reservation);
    }

    public void validateCreatableByAdmin(Reservation reservation) {
        validateAvailableSlot(reservation);
    }

    public void validateDeletableByUser(Reservation reservation, String name, LocalDateTime now) {
        validateOwner(reservation, name);
        validateNotPastForModification(reservation, now);
        validatePending(reservation);
    }

    public void validateUpdatableByUser(Reservation reservation, String name, LocalDateTime now) {
        validateOwner(reservation, name);
        validateNotPastForModification(reservation, now);
        validateConfirmed(reservation);
    }

    public void validatePaymentRetryByUser(Reservation reservation, String name, LocalDateTime now) {
        validateOwner(reservation, name);
        validateNotPastForModification(reservation, now);
        if (reservation.isConfirmed()) {
            throw new RoomescapeException(ErrorCode.PAYMENT_RETRY_NOT_ALLOWED,
                    "결제 대기 중인 예약만 재결제할 수 있습니다.");
        }
    }

    public void validateUpdatedReservation(Reservation reservation, Reservation updatedReservation, LocalDateTime now) {
        validateScheduleChanged(reservation, updatedReservation);
        validateNotPast(updatedReservation, now);
        validateAvailableSlot(updatedReservation);
    }

    private void validateNotPast(Reservation reservation, LocalDateTime now) {
        if (reservation.isPast(now)) {
            throw new RoomescapeException(ErrorCode.PAST_SCHEDULE, "이미 지난 시간으로는 예약할 수 없습니다.");
        }
    }

    private void validateAvailableSlot(Reservation reservation) {
        if (reservationRepository.existsBySlot(reservation.getSlot())) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESOURCE, "이미 예약된 시간입니다.");
        }
    }

    private void validateOwner(Reservation reservation, String name) {
        if (!reservation.isOwnedBy(new Reserver(name))) {
            throw new RoomescapeException(ErrorCode.FORBIDDEN_RESOURCE, "본인의 예약만 변경하거나 취소할 수 있습니다.");
        }
    }

    private void validateNotPastForModification(Reservation reservation, LocalDateTime now) {
        if (reservation.isPast(now)) {
            throw new RoomescapeException(ErrorCode.PAST_RESOURCE_LOCKED, "이미 지난 예약은 변경하거나 취소할 수 없습니다.");
        }
    }

    private void validateScheduleChanged(Reservation reservation, Reservation updatedReservation) {
        if (reservation.hasSameSchedule(updatedReservation)) {
            throw new RoomescapeException(ErrorCode.UNCHANGED_RESERVATION, "기존 예약과 같은 날짜·시간으로는 변경할 수 없습니다.");
        }
    }

    private void validatePending(Reservation reservation) {
        if (reservation.isConfirmed()) {
            throw new RoomescapeException(ErrorCode.PAYMENT_CANCELLATION_REQUIRED,
                    "결제가 완료된 예약은 결제 취소 후 삭제할 수 있습니다.");
        }
    }

    private void validateConfirmed(Reservation reservation) {
        if (reservation.isPending()) {
            throw new RoomescapeException(ErrorCode.PENDING_RESERVATION_LOCKED,
                    "결제 대기 중인 예약은 변경할 수 없습니다.");
        }
    }
}
