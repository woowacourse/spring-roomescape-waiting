package roomescape.reservation.service;

import jakarta.transaction.Transactional;
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

    public MyReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public List<MyReservationResponse> findAllMyReservationByMember(final Long memberId) {
        WaitingOrder waitingOrder = new WaitingOrder();

        return reservationRepository.findAllByMemberId(memberId)
                .stream()
                .map(reservation -> generateMyReservation(reservation, waitingOrder))
                .toList();
    }

    private MyReservationResponse generateMyReservation(
            final Reservation reservation,
            final WaitingOrder waitingOrder
    ) {
        if (reservation.isWaiting()) {
            return MyReservationResponse.from(reservation, waitingOrder.issueNextWaitingOrder());
        }
        return MyReservationResponse.from(reservation, WAITING_ORDER_START_VALUE);
    }
}
