package roomescape.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.Waiting;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Service
@Transactional
public class CancelReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public CancelReservationService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.getById(id);
        Optional<Waiting> priorityWaiting = waitingRepository.findFirstByReservationOrderByIdAsc(reservation);
        priorityWaiting.ifPresentOrElse(this::approve, () -> delete(reservation));
    }

    private void approve(Waiting waiting) {
        waiting.approve();
        waitingRepository.delete(waiting);
    }

    private void delete(Reservation reservation) {
        reservationRepository.deleteById(reservation.getId());
    }
}
