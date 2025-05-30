package roomescape.reservation.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.reservation.model.exception.ReservationException.AlreadyDoneWaitingException;
import roomescape.reservation.model.repository.ReservationWaitingRepository;
import roomescape.reservation.model.vo.Schedule;

@Component
@RequiredArgsConstructor
public class ReservationWaitingValidator {

    private final ReservationWaitingRepository reservationWaitingRepository;

    public void validateAlreadyWaiting(Schedule schedule, Long memberId) {
        boolean existsWaiting = reservationWaitingRepository.existsPendingByScheduleAndMemberId(schedule, memberId);
        if (existsWaiting) {
            throw new AlreadyDoneWaitingException();
        }
    }
}
