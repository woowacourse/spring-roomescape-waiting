package roomescape.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.domain.ReservationWaiting;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationWaitingRepository;

@Component
public class ReservationWaitingValidator {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingValidator(JpaReservationRepository reservationRepository,
                                       JpaReservationWaitingRepository reservationWaitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }


    public void validateWaiting(ReservationWaiting waiting) {
        validateNotPastDateAndTime(waiting);
        validateReservedSlot(waiting);
        validateNotOwnReservationSlot(waiting);
        validateNotDuplicateWaiting(waiting);
    }

    public void validateUpdatableReservation(ReservationWaiting waiting, String name) {
        validateOwner(waiting, name);
        validateReservationNotLocked(waiting);
    }

    private void validateNotOwnReservationSlot(ReservationWaiting waiting) {
        if (reservationRepository.existsByNameAndDateAndTime_IdAndTheme_Id(
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime().getId(),
                waiting.getTheme().getId())) {
            throw new BusinessException(ErrorCode.WAITING_NOT_ALLOWED_FOR_OWN_RESERVATION, "본인이 예약한 시간에는 대기를 신청할 수 없습니다.");
        }
    }

    private void validateReservedSlot(ReservationWaiting waiting) {
        if (!reservationRepository.existsByDateAndTime_IdAndTheme_Id(
                waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "예약 가능한 시간에는 대기를 신청할 수 없습니다.");
        }
    }

    private void validateNotPastDateAndTime(ReservationWaiting waiting) {
        LocalDateTime reservationDateTime = LocalDateTime.of(waiting.getDate(), waiting.getTime().getStartAt());
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PAST_RESERVATION, "이미 지난 시간으로는 예약 대기를 신청할 수 없습니다.");
        }
    }

    private void validateNotDuplicateWaiting(ReservationWaiting waiting) {
        if (reservationWaitingRepository.existsByNameAndDateAndTime_IdAndTheme_Id(
                waiting.getName(), waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION, "이미 예약 대기를 신청한 시간입니다.");
        }
    }

    private void validateOwner(ReservationWaiting waiting, String name) {
        if (!waiting.isOwnedBy(name)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_RESERVATION, "본인의 예약 대기만 취소할 수 있습니다.");
        }
    }

    private void validateReservationNotLocked(ReservationWaiting waiting) {
        if (waiting.isPast()) {
            throw new BusinessException(ErrorCode.PAST_RESERVATION_LOCKED, "이미 지난 예약 대기는 취소할 수 없습니다.");
        }
    }
}
