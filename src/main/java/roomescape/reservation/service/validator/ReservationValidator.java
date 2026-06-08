package roomescape.reservation.service.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.common.exception.DomainException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.repository.ReservationRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static roomescape.reservation.exception.ReservationErrorCode.*;

@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public void validateCreate(Reservation created) {
        validateNotDuplicated(created);
        validateNotPast(created);
    }

    public void validateBeforeEdit(Reservation original, LocalDate changedDate, Long changedTimeId, String guestName) {
        validateIsSameDateTime(original, changedDate, changedTimeId);
        validateIsMyReservation(guestName, original);
        validateAlreadyCanceled(original);
        validateAlreadyStarted(original);
    }

    public void validateEdit(Reservation changed) {
        validateNotPast(changed);
        validateNotDuplicated(changed);
    }

    public void validateCancel(Reservation canceled) {
        validateAlreadyCanceled(canceled);
        validateAlreadyStarted(canceled);
    }

    public void validateCancelMine(Reservation canceled, String guestName) {
        validateIsMyReservation(guestName, canceled);
        validateAlreadyCanceled(canceled);
        validateAlreadyStarted(canceled);
    }

    private void validateNotDuplicated(Reservation reservation) {
        if (reservationRepository.existsBySlotAndGuestNameExceptCanceled(
                reservation.getReservationSlot(),
                reservation.getGuestName()
        )) {
            throw new DomainException(RESERVATION_ALREADY_EXISTS);
        }
    }

    private static void validateIsSameDateTime(Reservation original, LocalDate changedDate, Long changedTimeId) {
        if (original.isSameDateTime(changedDate, changedTimeId)) {
            throw new DomainException(ReservationErrorCode.CANNOT_EDIT_SAME_DATE_TIME);
        }
    }

    /**
     * 변경된 예약 시간이 과거 시간일 때
     */
    private void validateNotPast(Reservation reservation) {
        if (reservation.isPassed(LocalDateTime.now(clock))) {
            throw new DomainException(PAST_RESERVATION_NOT_ALLOWED);
        }
    }

    /**
     * 이미 시작된 예약을 변경하려고 할 때
     */
    private void validateAlreadyStarted(Reservation reservation) {
        if (reservation.isPassed(LocalDateTime.now(clock))) {
            throw new DomainException(CANNOT_CHANGE_ALREADY_STARTED_RESERVATION);
        }
    }

    private static void validateAlreadyCanceled(Reservation canceled) {
        if (canceled.isCanceled()) {
            throw new DomainException(CANNOT_CHANGE_ALREADY_CANCELED);
        }
    }

    private void validateIsMyReservation(String guestName, Reservation reservation) {
        if (!reservation.isSameGuest(guestName)) {
            throw new DomainException(CANNOT_CHANGE_OTHER_GUEST_RESERVATION);
        }
    }
}
