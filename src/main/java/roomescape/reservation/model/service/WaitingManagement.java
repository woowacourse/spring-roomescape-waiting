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
        return waitingRepository
            .findAllByThemeAndDateAndTime(theme, date, time)
            .stream()
            .min(Comparator.comparing(Waiting::getId))
            .map(waiting -> {
                Reservation reservation = Reservation.builder()
                    .date(waiting.getDate())
                    .time(waiting.getTime())
                    .theme(waiting.getTheme())
                    .member(waiting.getMember())
                    .build();
                waitingRepository.delete(waiting);
                return reservation;
            });
    }
}
