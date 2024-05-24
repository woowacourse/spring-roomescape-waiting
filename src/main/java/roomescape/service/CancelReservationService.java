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
        List<WaitingWithSequence> waitings = waitingRepository.findWaitingsWithSequenceByReservation(reservation);

        Optional<WaitingWithSequence> priorityWaiting = waitings.stream()
                .filter(WaitingWithSequence::isPriority)
                .findFirst();

        priorityWaiting.ifPresentOrElse(it -> {
            Waiting waiting = it.getWaiting();
            waiting.approve();
            waitingRepository.delete(waiting);
        }, () -> reservationRepository.deleteById(reservation.getId()));
    }

    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 예약 정보 입니다.",
                        new Throwable("reservation_id : " + id)
                ));
    }
}
