package roomescape.reservation.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.repository.ReservationTimeRepository;

@Component
@RequiredArgsConstructor
public class ReservationTimeOperation {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationTimeValidator reservationTimeValidator;

    public void removeTime(ReservationTime reservationTime) {
        reservationTimeValidator.validateNotUsedInActive(reservationTime.getId());
        reservationTimeRepository.remove(reservationTime);
    }
}
