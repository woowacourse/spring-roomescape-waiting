package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
import roomescape.dto.reservation.AutoReservedFilter;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

import java.util.Optional;

@Service
@Transactional
public class AutoReserveService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public AutoReserveService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public Optional<ReservationResponse> reserveWaiting(AutoReservedFilter filter) {
        final Optional<Waiting> optionalWaiting = waitingRepository.findTopByDateAndTime_IdAndTheme_IdOrderById(
                filter.date(), filter.timeId(), filter.themeId());
        if (isNotExistsReservation(filter) && optionalWaiting.isPresent()) {
            final Waiting waiting = optionalWaiting.get();
            waitingRepository.deleteById(waiting.getId());
            Reservation saved = reservationRepository.save(waiting.toReservation());
            return Optional.of(ReservationResponse.from(saved));
        }
        return Optional.empty();
    }

    private boolean isNotExistsReservation(AutoReservedFilter filter) {
        boolean exists = reservationRepository.existsByDateAndTime_IdAndTheme_Id(
                filter.date(), filter.timeId(), filter.themeId());
        return !exists;
    }
}
