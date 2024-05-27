package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdetail.ReservationDetail;
import roomescape.exception.InvalidReservationException;

@Service
@Transactional
public class ReservationDeleteService {
    private final ReservationRepository reservationRepository;

    public ReservationDeleteService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void deleteById(long id) {
        reservationRepository.findById(id)
                .ifPresent(reservation -> {
                    deleteIfAvailable(reservation);
                    updateIfDeletedReserved(reservation);
                });
    }

    private void deleteIfAvailable(Reservation reservation) {
        validatePastReservation(reservation);
        reservationRepository.deleteById(reservation.getId());
    }

    private void validatePastReservation(Reservation reservation) {
        if (reservation.isReserved() && reservation.isPast()) {
            throw new InvalidReservationException("이미 지난 예약은 삭제할 수 없습니다.");
        }
    }

    private void updateIfDeletedReserved(Reservation reservation) {
        if (reservation.isReserved()) {
            ReservationDetail detail = reservation.getDetail();
            reservationRepository.findFirstByDetailIdOrderByCreatedAt(detail.getId())
                    .ifPresent(Reservation::reserved);
        }
    }
}
