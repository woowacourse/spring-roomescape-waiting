package roomescape.reservation.application.validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.exception.ReservationErrorCode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.dto.ReservationDetail;

@RequiredArgsConstructor
@Component
public class ReservationValidator {

    private final ReservationRepository reservationRepository;

    public void validateCreateDateTime(LocalDate date, LocalTime startAt, LocalDateTime currentDateTime) {
        if (LocalDateTime.of(date, startAt).isBefore(currentDateTime)) {
            throw new RoomEscapeException(ReservationErrorCode.PAST_RESERVATION_TIME);
        }
    }

    public void validateModification(
            String name,
            Reservation reservation,
            ReservationDetail reservationDetail,
            LocalDateTime currentDateTime
    ) {
        validateOwner(name, reservation);
        validateReservationNotPast(reservationDetail, currentDateTime);
    }

    public void validateUpdateSchedule(
            ReservationUpdateCommand request,
            Reservation reservation,
            LocalTime updatedStartAt,
            LocalDateTime currentDateTime
    ) {
        validateCreateDateTime(request.date(), updatedStartAt, currentDateTime);
        validateDuplicateReservation(request, reservation);
    }

    public void validateWaitingRequest(ReservationCreateCommand request) {
        boolean alreadyReservedBySameName = reservationRepository.existsByNameAndDateAndThemeAndTime(
                request.name(),
                request.date(),
                request.themeId(),
                request.timeId()
        );

        if (alreadyReservedBySameName) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void validateDuplicateReservation(ReservationUpdateCommand request, Reservation reservation) {
        boolean duplicated = reservationRepository.existsByDateAndThemeAndTimeExcludingId(
                request.date(),
                reservation.getThemeId(),
                request.timeId(),
                reservation.getId()
        );

        if (duplicated) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void validateOwner(String name, Reservation reservation) {
        if (!reservation.isOwner(name)) {
            throw new RoomEscapeException(ReservationErrorCode.FORBIDDEN_RESERVATION_ACCESS);
        }
    }

    private void validateReservationNotPast(
            ReservationDetail reservationDetail,
            LocalDateTime currentDateTime
    ) {
        LocalDateTime reservationDateTime = LocalDateTime.of(
                reservationDetail.date(),
                reservationDetail.startAt()
        );

        if (reservationDateTime.isBefore(currentDateTime)) {
            throw new RoomEscapeException(ReservationErrorCode.PAST_RESERVATION_MODIFICATION);
        }
    }
}
