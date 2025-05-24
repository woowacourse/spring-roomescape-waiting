package roomescape.application;

import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;

@Service
public class WaitingService {

    private final ReservationRepository reservationRepository;

    public WaitingService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public long countWaitingReservation(Reservation reservation) {
        if (!reservation.isWaiting()) {
            throw new IllegalArgumentException("예약 대기 상태가 아닙니다.");
        }
        return reservationRepository.countByReservationWaitingOrderByCreatedAt(
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getWaiting().getStatus(),
                reservation.getWaiting().getSavedDateTime()
        );
    }
}
