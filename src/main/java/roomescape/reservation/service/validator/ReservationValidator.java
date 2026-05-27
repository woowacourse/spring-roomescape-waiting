package roomescape.reservation.service.validator;

import static roomescape.reservation.exception.ReservationErrorCode.CANNOT_EDIT_ALREADY_STARTED_RESERVATION;
import static roomescape.reservation.exception.ReservationErrorCode.CANNOT_EDIT_OTHER_GUEST_RESERVATION;
import static roomescape.reservation.exception.ReservationErrorCode.PAST_RESERVATION_NOT_ALLOWED;
import static roomescape.reservation.exception.ReservationErrorCode.RESERVATION_ALREADY_CANCELED;
import static roomescape.reservation.exception.ReservationErrorCode.RESERVATION_ALREADY_EXISTS;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.common.exception.DomainException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;

@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public void validateCreate(Reservation created) {
        validateNotDuplicatedExcept(created);
        validateNotPast(created);
    }

    public void validateEdit(Reservation original, Reservation changed, String guestName) {
        validateIsMyReservation(guestName, original);
        validateNotCanceled(original);
        validateAlreadyStarted(original);
        validateNotPast(changed);
        validateNotDuplicatedExcept(changed);
    }

    public void validateDelete(Reservation deleted, String guestName) {
        validateIsMyReservation(guestName, deleted);
        validateNotCanceled(deleted);
        validateAlreadyStarted(deleted);
    }

    private void validateNotDuplicatedExcept(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndGuestNameExceptCanceled(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getGuestName()
        )) {
            throw new DomainException(RESERVATION_ALREADY_EXISTS);
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

    private void validateNotCanceled(Reservation reservation) {
        if (reservation.isCanceled()) {
            throw new DomainException(RESERVATION_ALREADY_CANCELED);
        }
    }

    private void validateIsMyReservation(String guestName, Reservation reservation) {
        if (!reservation.isSameGuest(guestName)) {
            throw new DomainException(CANNOT_EDIT_OTHER_GUEST_RESERVATION);
        }
    }
}
