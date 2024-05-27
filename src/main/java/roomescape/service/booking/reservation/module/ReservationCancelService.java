package roomescape.service.booking.reservation.module;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Status;
import roomescape.domain.waiting.Waiting;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Service
@Transactional
public class ReservationCancelService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationCancelService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public void deleteReservation(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        List<Reservation> waitingReservations = findWaitingReservation(reservation);

        reservationRepository.delete(reservation);
        adjustWaitingOrder(waitingReservations);
    }

    private List<Reservation> findWaitingReservation(Reservation reservation) {
        return reservationRepository.findByDateAndTimeIdAndThemeIdAndStatus(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                Status.WAITING
        );
    }

    private void adjustWaitingOrder(List<Reservation> reservationsToAdjust) {
        for (Reservation reservation : reservationsToAdjust) {
            Waiting waiting = findWaitingByReservationId(reservation.getId());
            if (waiting.isFirstOrder()) {
                reservation.changeStatusToReserve();
                waitingRepository.delete(waiting);
            }
            if (!waiting.isFirstOrder()) {
                waiting.decreaseWaitingOrderByOne();
            }
        }
    }

    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 예약 정보 입니다.",
                        new Throwable("reservation_id : " + reservationId)
                ));
    }

    private Waiting findWaitingByReservationId(Long reservationId) {
        return waitingRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 예약 정보와 일치하는 대기 정보가 존재하지 않습니다.",
                        new Throwable("reservation_id : " + reservationId)
                ));
    }
}
