package roomescape.reservation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.custom.reason.reservation.ReservationNotFoundException;
import roomescape.reservation.reservation.Reservation;
import roomescape.reservation.reservation.ReservationRepository;
import roomescape.reservation.waiting.Waiting;
import roomescape.reservation.waiting.WaitingRepository;
import roomescape.schedule.Schedule;

import java.util.List;

@Service
@AllArgsConstructor
public class ReservationWaitingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public void deleteReservationById(final Long id) {
        Reservation reservation = getReservationById(id);
        reservationRepository.deleteById(id);

        Schedule schedule = reservation.getSchedule();

        List<Waiting> waitings = waitingRepository.findAllBySchedule(schedule);
        if (waitings.isEmpty()) {
            return;
        }

        changeFirstWaitingToReservation(waitings);
        waitings.forEach(Waiting::decrementRank);
    }

    private void changeFirstWaitingToReservation(final List<Waiting> waitings) {
        Waiting firstWaiting = waitings.getFirst();
        Reservation reservation = new Reservation(firstWaiting.getMember(), firstWaiting.getSchedule());
        reservationRepository.save(reservation);
        waitingRepository.delete(firstWaiting);
    }

    private Reservation getReservationById(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);
    }
}
