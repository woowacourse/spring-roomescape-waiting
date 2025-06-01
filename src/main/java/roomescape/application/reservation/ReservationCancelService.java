package roomescape.application.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.support.exception.NotFoundEntityException;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;

@Service
public class ReservationCancelService {

    private final ReservationRepository reservationRepository;
    private final WaitingPromotion waitingPromotion;

    public ReservationCancelService(ReservationRepository reservationRepository,
                                    WaitingPromotion waitingPromotion) {
        this.reservationRepository = reservationRepository;
        this.waitingPromotion = waitingPromotion;
    }

    @Transactional
    public void cancel(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservationRepository.deleteById(reservationId);
        waitingPromotion.promoteTopWaiting(reservation.getReservationSlot());
    }

    private Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundEntityException("해당 예약이 존재하지 않습니다."));
    }
}
