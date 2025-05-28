package roomescape.reservation.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
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
        AtomicLong waitingOrder = new AtomicLong(WAITING_ORDER_START_VALUE);

        return reservationRepository.findAllByMemberId(memberId)
                .stream()
                .map(reservation -> generateMyReservationResponse(reservation, waitingOrder))
                .toList();
    }

    private MyReservationResponse generateMyReservationResponse(
            final Reservation reservation,
            final AtomicLong waitingOrder
    ) {
        if (reservation.isWaiting()) {
            return MyReservationResponse.from(reservation, waitingOrder.incrementAndGet());
        }
        return MyReservationResponse.from(reservation, WAITING_ORDER_START_VALUE);
    }
}
