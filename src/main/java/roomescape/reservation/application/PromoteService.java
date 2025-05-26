package roomescape.reservation.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.domain.Waitings;

@Service
@AllArgsConstructor
@Transactional
public class PromoteService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void promoteWaiting(Reservation reservation) {
        ReservationSpec spec = reservation.getSpec();
        Waitings waitings = new Waitings(waitingRepository.findBySpec(spec));
        Waiting waiting = waitings.pollHighestPriority();

        if (waiting == null) {
            return;
        }

        Reservation newReservation = new Reservation(waiting.getMember(), spec);
        waitingRepository.deleteById(waiting.getId());
        reservationRepository.save(newReservation);
    }
}
