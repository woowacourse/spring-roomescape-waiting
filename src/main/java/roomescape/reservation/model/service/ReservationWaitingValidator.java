package roomescape.reservation.model.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.exception.ReservationException.AlreadyDoneWaitingException;
import roomescape.reservation.model.repository.ReservationWaitingRepository;

@Component
@RequiredArgsConstructor
public class ReservationWaitingValidator {

    private final ReservationWaitingRepository reservationWaitingRepository;

    public void validateAlreadyWaiting(LocalDate date, Long timeId, Long themeId, Long memberId) {
        ReservationWaiting reservationWaiting = reservationWaitingRepository.getByDateAndTimeIdAndThemeId(date,
                timeId, themeId);
        if (reservationWaiting.hasNotEqualsMemberId(memberId)) {
            throw new AlreadyDoneWaitingException();
        }
    }
}
