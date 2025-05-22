package roomescape.reservation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.custom.reason.reservation.ReservationNotFoundException;
import roomescape.reservation.reservation.Reservation;
import roomescape.reservation.reservation.ReservationRepository;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;
import roomescape.reservation.waiting.Waiting;
import roomescape.reservation.waiting.WaitingRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ReservationWaitingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public void deleteReservationById(final Long id) {
        Reservation reservation = getReservationById(id);
        reservationRepository.deleteById(id);

        LocalDate date = reservation.getDate();
        ReservationTime reservationTime = reservation.getReservationTime();
        Theme theme = reservation.getTheme();

        List<Waiting> waitings = waitingRepository.findByReservationSlot(date, reservationTime, theme);
        if (waitings.isEmpty()) {
            return;
        }

        changeFirstWaitingToReservation(waitings);
        waitings.forEach(Waiting::decrementRank);
    }

    private void changeFirstWaitingToReservation(final List<Waiting> waitings) {
        Waiting firstWaiting = waitings.getFirst();
        firstWaiting.getReservation().confirmReservation();
        waitingRepository.delete(firstWaiting);
    }

    private Reservation getReservationById(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);
    }
}
