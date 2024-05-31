package roomescape.reservation.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
public class ReservationAndWaitingQueryService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationAndWaitingQueryService(final ReservationRepository reservationRepository,
            final WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<MyReservationResponse> findAllByMemberId(final Long memberId) {
        List<MyReservationResponse> myReservationResponses = new ArrayList<>();
        myReservationResponses.addAll(
                reservationRepository.findAllByMemberId(memberId).stream()
                        .map(MyReservationResponse::new)
                        .toList());
        myReservationResponses.addAll(
                waitingRepository.findWaitingWithRanksByMemberId(memberId).stream()
                        .map(MyReservationResponse::new)
                        .toList());

        return myReservationResponses;
    }
}
