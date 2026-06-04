package roomescape.reservation.application;

import org.springframework.stereotype.Component;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.waiting.application.WaitingReference;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;

@Component
public class WaitingReferenceAdapter implements WaitingReference {

    private final ReservationRepository reservationRepository;

    public WaitingReferenceAdapter(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void validateExistReservation(WaitingCreateCommand waitingCreateCommand) {
        if (reservationRepository.findByDateAndTimeIdAndThemeIdForUpdate(
                waitingCreateCommand.date(),
                waitingCreateCommand.timeId(),
                waitingCreateCommand.themeId()).isEmpty()) {
            throw new BusinessException(WaitingErrorCode.WAITING_NOT_EXIST_RESERVATION);
        }
    }

    @Override
    public void promoteToReservation(Waiting waiting) {
        Reservation reservation = Reservation.create(
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme()
        );
        reservationRepository.save(reservation);
    }
}
