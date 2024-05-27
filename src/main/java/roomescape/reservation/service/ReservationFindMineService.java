package roomescape.reservation.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class ReservationFindMineService {
    private static final Comparator<MyReservationResponse> RESERVATION_SORTING_COMPARATOR = Comparator
            .comparing(MyReservationResponse::date).thenComparing(MyReservationResponse::startAt);

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationFindMineService(ReservationRepository reservationRepository,
                                      WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> findMyReservations(Long memberId) {
        List<MyReservationResponse> reservations = findReservations(memberId);
        List<MyReservationResponse> waitings = findWaitings(memberId);

        return makeMyReservations(reservations, waitings);
    }

    private List<MyReservationResponse> findReservations(Long memberId) {
        return reservationRepository.findByMemberId(memberId)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    private List<MyReservationResponse> findWaitings(Long memberId) {
        return waitingRepository.findByMemberId(memberId)
                .stream()
                .map(waiting -> MyReservationResponse.from(waiting, countOrderOfWaiting(waiting)))
                .toList();
    }

    private Long countOrderOfWaiting(Waiting waiting) {
        return waitingRepository.countByReservationAndCreatedAtLessThanEqual(
                waiting.getReservation(), waiting.getCreatedAt());
    }

    private List<MyReservationResponse> makeMyReservations(List<MyReservationResponse> reservations,
                                                           List<MyReservationResponse> waitings) {
        List<MyReservationResponse> response = new ArrayList<>();
        response.addAll(reservations);
        response.addAll(waitings);
        response.sort(RESERVATION_SORTING_COMPARATOR);
        return response;
    }
}
