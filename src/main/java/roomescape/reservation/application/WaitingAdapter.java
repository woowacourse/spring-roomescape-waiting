package roomescape.reservation.application;

import org.springframework.stereotype.Component;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.waiting.application.WaitingReference;
import roomescape.waiting.application.dto.WaitingCreateCommand;

@Component
public class WaitingAdapter implements WaitingReference {

    private final ReservationRepository reservationRepository;

    public WaitingAdapter(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void validateExistReservation(WaitingCreateCommand waitingCreateCommand) {
        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                        waitingCreateCommand.date(),
                        waitingCreateCommand.timeId(),
                        waitingCreateCommand.themeId())
                .orElseThrow(() -> new BusinessException(WaitingErrorCode.WAITING_NOT_EXIST_RESERVATION));

        if (reservation.getName().equals(waitingCreateCommand.name())) {
            throw new BusinessException(WaitingErrorCode.WAITING_ALREADY_EXISTS);
        }
    }
}
