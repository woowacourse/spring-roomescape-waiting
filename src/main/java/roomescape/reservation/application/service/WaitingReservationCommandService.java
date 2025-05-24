package roomescape.reservation.application.service;

import org.springframework.stereotype.Service;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.domain.WaitingReservationRepository;

@Service
public class WaitingReservationCommandService {

    private final WaitingReservationRepository waitingReservationRepository;

    public WaitingReservationCommandService(WaitingReservationRepository waitingReservationRepository) {
        this.waitingReservationRepository = waitingReservationRepository;
    }

    public WaitingReservation save(final WaitingReservation waitingReservation) {
        return waitingReservationRepository.save(waitingReservation);
    }
}
