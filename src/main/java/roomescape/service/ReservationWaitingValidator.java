package roomescape.service;

import org.springframework.stereotype.Component;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Reserver;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationWaitingRepository;

import java.time.LocalDateTime;

@Component
public class ReservationWaitingValidator {

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingValidator(ReservationRepository reservationRepository,
                                       ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }


    public void validateWaiting(ReservationWaiting waiting, LocalDateTime now) {
        validateNotPast(waiting, now);
        validateAvailableSlot(waiting);
        validateNotOwnReservationSlot(waiting);
        validateNotDuplicateWaiting(waiting);
    }

    public void validateModifiable(ReservationWaiting waiting, String name, LocalDateTime now) {
        validateOwner(waiting, name);
        validateNotPastForModification(waiting, now);
    }

    private void validateNotPast(ReservationWaiting waiting, LocalDateTime now) {
        if (waiting.isPast(now)) {
            throw new RoomescapeException(ErrorCode.PAST_SCHEDULE, "이미 지난 시간으로는 예약 대기를 신청할 수 없습니다.");
        }
    }

    private void validateAvailableSlot(ReservationWaiting waiting) {
        if (!reservationRepository.existsBySlotForUpdate(waiting.getSlot())) {
            throw new RoomescapeException(ErrorCode.INVALID_INPUT, "예약 가능한 시간에는 대기를 신청할 수 없습니다.");
        }
    }

    private void validateNotOwnReservationSlot(ReservationWaiting waiting) {
        if (reservationRepository.existsByReserverAndSlot(waiting.getReserver(), waiting.getSlot())) {
            throw new RoomescapeException(
                    ErrorCode.WAITING_NOT_ALLOWED_FOR_OWN_RESERVATION, "본인이 예약한 시간에는 대기를 신청할 수 없습니다."
            );
        }
    }

    private void validateNotDuplicateWaiting(ReservationWaiting waiting) {
        if (reservationWaitingRepository.existsByReserverAndSlot(waiting.getReserver(), waiting.getSlot())) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESOURCE, "이미 예약 대기를 신청한 시간입니다.");
        }
    }

    private void validateOwner(ReservationWaiting waiting, String name) {
        if (!waiting.isOwnedBy(new Reserver(name))) {
            throw new RoomescapeException(ErrorCode.FORBIDDEN_RESOURCE, "본인의 예약 대기만 취소할 수 있습니다.");
        }
    }

    private void validateNotPastForModification(ReservationWaiting waiting, LocalDateTime now) {
        if (waiting.isPast(now)) {
            throw new RoomescapeException(ErrorCode.PAST_RESOURCE_LOCKED, "이미 지난 예약 대기는 취소할 수 없습니다.");
        }
    }
}
