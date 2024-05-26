package roomescape.domain.reservation;

import java.util.List;
import org.springframework.stereotype.Component;
import roomescape.repository.ReservationRepository;

@Component
public class RankCalculator {

    private final ReservationRepository reservationRepository;

    public RankCalculator(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public int calculate(Reservation waiting) {
        List<Reservation> reservations = reservationRepository.findAllByDateAndTimeIdAndThemeId(
            waiting.getDate(),
            waiting.getTime().getId(),
            waiting.getTheme().getId());

        long count = reservations.stream()
            .filter(reservation -> reservation.getId() < waiting.getId())
            .count();
        return (int) count;
    }
}
