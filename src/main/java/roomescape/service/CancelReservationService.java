package roomescape.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithSequence;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Service
public class CancelReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public CancelReservationService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = findReservationById(id);
        Optional<WaitingWithSequence> priorityWaiting = findPriorityWaiting(reservation);
        priorityWaiting.ifPresentOrElse(this::approve, () -> delete(reservation));
    }

    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 예약 정보 입니다.",
                        new Throwable("reservation_id : " + id)
                ));
    }

    private Optional<WaitingWithSequence> findPriorityWaiting(Reservation reservation) {
        List<WaitingWithSequence> waitings = waitingRepository.findWaitingsWithSequenceByReservation(reservation);

        return waitings.stream()
                .filter(WaitingWithSequence::isPriority)
                .findFirst();
    }

    private void approve(WaitingWithSequence waitingWithSequence) {
        Waiting waiting = waitingWithSequence.getWaiting();
        waiting.approve();
        waitingRepository.delete(waiting);
    }

    private void delete(Reservation reservation) {
        reservationRepository.deleteById(reservation.getId());
    }
}
