package roomescape.service;

import org.springframework.stereotype.Component;
import roomescape.domain.ReservationWaiting;
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


    public void validateWaiting(ReservationWaiting waiting) {
        validateNotPastDateAndTime(waiting);
        validateAlreadyReserved(waiting);
        validateNotOwnReservationSlot(waiting);
        validateNotDuplicateWaiting(waiting);
    }

    public void validateUpdatableReservation(ReservationWaiting waiting, String name) {
        validateOwner(waiting, name);
        validateReservationNotLocked(waiting);
    }

    private void validateNotOwnReservationSlot(ReservationWaiting waiting) {
        if (reservationRepository.existsByNameWith(
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime().getId(),
                waiting.getTheme().getId())) {
            throw new RoomescapeException(ErrorCode.WAITING_NOT_ALLOWED_FOR_OWN_RESERVATION, "본인이 예약한 시간에는 대기를 신청할 수 없습니다.");
        }
    }

    private void validateAlreadyReserved(ReservationWaiting waiting) {
        if (!reservationRepository.existsWithForUpdate(
                waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId())) {
            throw new RoomescapeException(ErrorCode.INVALID_INPUT, "예약 가능한 시간에는 대기를 신청할 수 없습니다.");
        }
    }

    private void validateNotPastDateAndTime(ReservationWaiting waiting) {
        LocalDateTime reservationDateTime = LocalDateTime.of(waiting.getDate(), waiting.getTime().getStartAt());
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new RoomescapeException(ErrorCode.PAST_SCHEDULE, "이미 지난 시간으로는 예약 대기를 신청할 수 없습니다.");
        }
    }

    private void validateNotDuplicateWaiting(ReservationWaiting waiting) {
        if (reservationWaitingRepository.existsByNameWith(
                waiting.getName(), waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId())) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESOURCE, "이미 예약 대기를 신청한 시간입니다.");
        }
    }

    private void validateOwner(ReservationWaiting waiting, String name) {
        if (!waiting.isOwnedBy(name)) {
            throw new RoomescapeException(ErrorCode.FORBIDDEN_RESOURCE, "본인의 예약 대기만 취소할 수 있습니다.");
        }
    }

    private void validateReservationNotLocked(ReservationWaiting waiting) {
        if (waiting.isPast()) {
            throw new RoomescapeException(ErrorCode.PAST_RESOURCE_LOCKED, "이미 지난 예약 대기는 취소할 수 없습니다.");
        }
    }
}
