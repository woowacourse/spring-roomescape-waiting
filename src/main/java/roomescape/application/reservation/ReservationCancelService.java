package roomescape.application.reservation;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRepository;

@Service
public class ReservationCancelService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationCancelService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public void cancel(Long reservationId) {
        Optional<Waiting> findWaiting = findTopStartedWaiting(reservationId);
        reservationRepository.deleteById(reservationId);
        findWaiting.ifPresent(waiting -> {
            Reservation reservation = waiting.toReservation();
            reservationRepository.save(reservation);
            waitingRepository.delete(waiting);
        });
    }

    private Optional<Waiting> findTopStartedWaiting(Long reservationId) {
        return reservationRepository.findReservationSlotById(reservationId)
                .flatMap(waitingRepository::findTopByReservationSlotOrderByStartedAtAsc);
    }
}
