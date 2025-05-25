package roomescape.reservation.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
public class AutoReserveService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public AutoReserveService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public void addReservationFromWaiting(Reservation reservation) {
        Optional<Waiting> waiting = waitingRepository.findFirstWaitingByDetails_DateAndDetails_Time_IdAndDetails_Theme_Id(reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        if (waiting.isPresent()) {
            Waiting targetWaiting = waiting.get();
            Reservation newReservation = convertWaitingToReservation(targetWaiting);
            waitingRepository.deleteById(targetWaiting.getId());
            reservationRepository.save(newReservation);
        }
    }

    private Reservation convertWaitingToReservation(Waiting waiting) {
        return new Reservation(null, waiting.getMember(), waiting.getDate(), waiting.getTime(), waiting.getTheme());
    }
}
