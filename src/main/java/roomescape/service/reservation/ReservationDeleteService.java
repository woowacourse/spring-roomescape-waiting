package roomescape.service.reservation;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationRepository;

@Service
public class ReservationDeleteService {

    private final ReservationRepository reservationRepository;

    public ReservationDeleteService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void deleteReservation(long id) {
        Reservation deleteReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 아이디 입니다."));
        reservationRepository.deleteById(id);

        if (deleteReservation.isReserved()) {
            reservationRepository.findNextWaiting(deleteReservation.getTheme(),
                            deleteReservation.getReservationTime(), deleteReservation.getDate(), Limit.of(1))
                    .ifPresent(Reservation::changeWaitingToReserved);
        }
    }
}
