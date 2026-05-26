package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationWaiting;
import roomescape.repository.ReservationWaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    @Transactional
    public ReservationWaiting addWaiting(ReservationWaiting reservationWaiting) {
        return reservationWaitingRepository.save(reservationWaiting);
    }
}
