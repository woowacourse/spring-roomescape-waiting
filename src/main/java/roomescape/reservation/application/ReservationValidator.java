package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.global.exception.ReservationErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;

@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final ReservationRepository reservationRepository;

    public void validateAlreadyReservation(ReservationCreateCommand createCommand) {
        boolean exists = reservationRepository
                .findByDateAndTimeIdAndThemeId(createCommand.date(), createCommand.timeId(), createCommand.themeId())
                .isPresent();
        if (exists) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS);
        }
    }

    public void validateAlreadyReservationExcludingSelf(
            ReservationUpdateCommand updateCommand, Reservation targetReservation
    ) {
        Long themeId = targetReservation.getTheme().getId();
        reservationRepository.findByDateAndTimeIdAndThemeId(updateCommand.date(), updateCommand.timeId(), themeId)
                .filter(foundReservation -> !foundReservation.equals(targetReservation))
                .ifPresent(reservation -> {
                    throw new BusinessException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS);
                });
    }
}
