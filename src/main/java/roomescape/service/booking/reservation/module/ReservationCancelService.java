package roomescape.service.booking.reservation.module;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.repository.ReservationRepository;

@Service
@Transactional
public class ReservationCancelService {

    private final ReservationRepository reservationRepository;

    public ReservationCancelService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void deleteReservation(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        reservationRepository.delete(reservation);
    }

    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 예약 정보 입니다.",
                        new Throwable("reservation_id : " + reservationId)
                ));
    }
}
