package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Waiting;
import roomescape.dto.reservation.AutoReservedFilter;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

import java.util.Optional;

@Service
public class AutoReserveService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public AutoReserveService(final ReservationRepository reservationRepository,
                              final WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public void reserveWaiting(AutoReservedFilter filter) {
        if (isNotExistsReservation(filter)) {
            final Optional<Waiting> waiting = waitingRepository.findTopByDateAndTime_IdAndTheme_IdOrderById(
                    filter.date(), filter.timeId(), filter.themeId());
            waiting.ifPresent(this::changeToReservation);
        }
    }

    private boolean isNotExistsReservation(AutoReservedFilter filter) {
        return !reservationRepository.existsByDateAndTime_IdAndTheme_Id(
                filter.date(), filter.timeId(), filter.themeId());
    }

    private void changeToReservation(Waiting waiting) {
        waitingRepository.deleteById(waiting.getId());
        reservationRepository.save(waiting.toReservation());
    }
}
