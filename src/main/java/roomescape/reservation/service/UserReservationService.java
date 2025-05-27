package roomescape.reservation.service;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
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

    public List<UserReservationResponse> findAllMemberReservations(Member member) {
        List<UserReservationResponse> reservations = findAllReservationByMember(member);
        List<UserReservationResponse> waitings = findAllWaitingByMember(member);
        return mergeList(reservations, waitings);
    }

    private List<UserReservationResponse> findAllWaitingByMember(Member member) {
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingWithRankByMemberId(member.getId());
        List<UserReservationResponse> waitingResponses = waitingWithRanks.stream()
                .map(UserReservationResponse::from)
                .toList();
        return waitingResponses;
    }

    private List<UserReservationResponse> findAllReservationByMember(Member member) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(member.getId());
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
