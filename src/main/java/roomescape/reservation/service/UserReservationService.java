package roomescape.reservation.service;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.UserReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.waiting.dto.WaitingWithRank;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class UserReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public UserReservationService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<UserReservationResponse> findAllMemberReservations(Long memberId) {
        List<UserReservationResponse> reservations = findAllReservationByMemberId(memberId);
        List<UserReservationResponse> waitings = findAllWaitingByMemberId(memberId);
        return mergeList(reservations, waitings);
    }

    private List<UserReservationResponse> findAllWaitingByMemberId(Long memberId) {
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingWithRankByMemberId(memberId);
        List<UserReservationResponse> waitingResponses = waitingWithRanks.stream()
                .map(UserReservationResponse::from)
                .toList();
        return waitingResponses;
    }

    private List<UserReservationResponse> findAllReservationByMemberId(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);
        return reservations.stream()
                .map(UserReservationResponse::from)
                .toList();
    }

    private List<UserReservationResponse> mergeList(List<UserReservationResponse>... lists) {
        return Stream.of(lists)
                .flatMap(List::stream)
                .toList();
    }
}
