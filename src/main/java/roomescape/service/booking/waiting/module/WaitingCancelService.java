package roomescape.service.booking.waiting.module;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.Waiting;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Service
@Transactional
public class WaitingCancelService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    public WaitingCancelService(WaitingRepository waitingRepository, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
    }

    public void cancelWaitingForUser(Long reservationId) {
        Waiting waiting = findWaitingByReservationId(reservationId);
        deleteWaiting(waiting.getId());
    }

    public void deleteWaiting(Long waitingId) {
        Waiting waiting = findWaitingById(waitingId);
        Reservation reservation = waiting.getReservation();

        List<Reservation> waitingReservations = findWaitingReservationBySameConditions(reservation);
        adjustWaitingOrder(waitingReservations, waiting.getWaitingOrderValue());
        waitingRepository.delete(waiting);
        reservationRepository.delete(reservation);
    }

    private List<Reservation> findWaitingReservationBySameConditions(Reservation reservation) {
        return reservationRepository.findByDateAndTimeIdAndThemeIdAndStatus(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getStatus()
        );
    }

    private void adjustWaitingOrder(List<Reservation> reservationsToAdjust, int waitingOrderToDelete) {
        for (Reservation reservation : reservationsToAdjust) {
            Waiting waiting = findWaitingByReservationId(reservation.getId());
            if (waiting.isWaitingOrderGreaterThan(waitingOrderToDelete)) {
                updateWaitingStatus(reservation, waiting);
            }
        }
    }

    private void updateWaitingStatus(Reservation reservation, Waiting waiting) {
        if (waiting.isFirstOrder()) {
            waitingRepository.delete(waiting);
            reservation.changeStatusToReserve();
        }
        if (!waiting.isFirstOrder()) {
            waiting.decreaseWaitingOrderByOne();
        }
    }

    private Waiting findWaitingByReservationId(Long reservationId) {
        return waitingRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 예약 정보와 일치하는 대기 정보가 존재하지 않습니다.",
                        new Throwable("reservation_id : " + reservationId)
                ));
    }

    private Waiting findWaitingById(Long waitingId) {
        return waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 예약 대기 정보가 존재하지 않습니다.",
                        new Throwable("waiting_id : " + waitingId)
                ));
    }
}
