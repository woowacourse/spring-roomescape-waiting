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
        validateAlreadyStarted(original);
    }

    public void validateEdit(Reservation changed) {
        validateNotPast(changed);
        validateNotDuplicated(changed);
    }

    public void validateDelete(Reservation deleted, String guestName) {
        validateIsMyReservation(guestName, deleted);
        validateAlreadyStarted(deleted);
    }

    private void validateNotDuplicated(Reservation reservation) {
        if (reservationRepository.existsBySlotAndGuestNameExceptCanceled(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getGuestName()
        )) {
            throw new DomainException(RESERVATION_ALREADY_EXISTS);
        }
    }

    private static void validateIsSameDateTime(Reservation original, LocalDate changedDate, Long changedTimeId) {
        if(original.isSameDateTime(changedDate, changedTimeId)) {
            throw new DomainException(ReservationErrorCode.CANNOT_EDIT_SAME_DATE_TIME);
        }
    }

    private void validateNotPast(Reservation reservation) {
        if (reservation.isPassed(LocalDateTime.now(clock))) {
            throw new DomainException(PAST_RESERVATION_NOT_ALLOWED);
        }
    }

    private void validateAlreadyStarted(Reservation reservation) {
        if (reservation.isPassed(LocalDateTime.now(clock))) {
            throw new DomainException(CANNOT_EDIT_ALREADY_STARTED_RESERVATION);
        }
    }

    private void validateIsMyReservation(String guestName, Reservation reservation) {
        if (!reservation.isSameGuest(guestName)) {
            throw new DomainException(CANNOT_EDIT_OTHER_GUEST_RESERVATION);
        }
    }
}
