package roomescape.reservation.model.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.repository.WaitingRepository;

@Component
@RequiredArgsConstructor
public class WaitingManagement {

    private final WaitingRepository waitingRepository;

    public Optional<Reservation> promoteWaiting(ReservationTheme theme, LocalDate date,
        ReservationTime time) {
        Optional<Waiting> waiting = waitingRepository
            .findAllByThemeAndDateAndTime(theme, date, time)
            .stream()
            .min(Comparator.comparing(Waiting::getId));

        if (waiting.isEmpty()) {
            return Optional.empty();
        }

        Waiting waitingToPromote = waiting.get();

        Reservation promotedReservation = Reservation.builder()
            .date(waitingToPromote.getDate())
            .time(waitingToPromote.getTime())
            .theme(waitingToPromote.getTheme())
            .member(waitingToPromote.getMember())
            .build();

        waitingRepository.delete(waitingToPromote);

        return Optional.of(promotedReservation);
    }
}
