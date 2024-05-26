package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Status;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.waiting.WaitingResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Service
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public WaitingService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<WaitingResponse> getAllWaitingReservations() {
        List<Waiting> allWaiting = waitingRepository.findAll();
        return allWaiting.stream()
                .map(WaitingResponse::from)
                .toList();
    }
    
    public void deleteWaitingForUser(Long reservationId) {
        Waiting waiting = findWaitingByReservationId(reservationId);
        deleteWaiting(waiting.getId());
    }

    public void deleteWaiting(Long waitingId) {
        Waiting waiting = findWaitingById(waitingId);
        Reservation reservation = waiting.getReservation();
        validateDeletable(reservation);

        List<Reservation> waitingReservations = findWaitingReservationBySameCriteria(reservation);
        adjustWaitingOrder(waitingReservations, waiting.getWaitingOrderValue());
        waitingRepository.delete(waiting);
        reservationRepository.delete(reservation);
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

    private List<Reservation> findWaitingReservationBySameCriteria(Reservation reservation) {
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
            if (waiting.getWaitingOrderValue() > waitingOrderToDelete) {
                updateWaitingStatus(reservation, waiting);
            }
        }
    }

    private void updateWaitingStatus(Reservation reservation, Waiting waiting) {
        if (waiting.getWaitingOrderValue() == 1) {
            waitingRepository.delete(waiting);
            reservation.changeStatusToReserve();
        }
        if (waiting.getWaitingOrderValue() > 1) {
            waiting.decreaseWaitingOrderByOne();
        }
    }

    private void validateDeletable(Reservation reservation) {
        if (reservation.getStatus() == Status.RESERVED) {
            throw new IllegalArgumentException("[ERROR] 이미 확정된 예약은 취소가 불가능합니다.");
        }
    }
}
