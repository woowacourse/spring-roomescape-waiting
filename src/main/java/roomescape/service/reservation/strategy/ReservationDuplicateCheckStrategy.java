package roomescape.service.reservation.strategy;

import org.springframework.stereotype.Component;
import roomescape.domain.reservation.Reservation;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.repository.reservation.ReservationRepository;

@Component
public class ReservationDuplicateCheckStrategy implements ReservationValidateStrategy {

    private final ReservationRepository reservationRepository;

    public ReservationDuplicateCheckStrategy(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void addReservationValidate(Reservation reservation) {
        boolean exists = reservationRepository.existsByDateAndTimeAndTheme(
                reservation.getDate(),
                reservation.getReservationTime(),
                reservation.getTheme()
        );
        if (exists) {
            throw new InvalidReservationException("중복된 예약신청입니다");
        }
    }
}
