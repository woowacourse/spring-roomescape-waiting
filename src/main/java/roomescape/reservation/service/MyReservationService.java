package roomescape.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.WaitingOrder;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.dto.MyReservationResponse;

@Service
public class MyReservationService {
    private static final long WAITING_ORDER_START_VALUE = 0;

    private final ReservationRepository reservationRepository;
    private final WaitingOrder waitingOrder;

    public MyReservationService(ReservationRepository reservationRepository, WaitingOrder waitingOrder) {
        this.reservationRepository = reservationRepository;
        this.waitingOrder = waitingOrder;
    }

    public List<MyReservationResponse> findAllMyReservationByMemberId(final Long memberId) {
        waitingOrder.resetWaitingOrder();
        
        return reservationRepository.findAllByMemberId(memberId)
                .stream()
                .map(this::generateMyReservation)
                .toList();
    }

    private MyReservationResponse generateMyReservation(Reservation reservation) {
        if (reservation.isWaitingStatus()) {
            return MyReservationResponse.from(reservation, waitingOrder.issueNextWaitingOrder());
        }
        return MyReservationResponse.from(reservation, WAITING_ORDER_START_VALUE);
    }
}
