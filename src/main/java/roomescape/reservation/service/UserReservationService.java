package roomescape.reservation.service;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.UserReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.waiting.domain.WaitingWithRank;
import roomescape.waiting.servcie.WaitingService;

@Service
public class UserReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingService waitingService;

    public UserReservationService(ReservationRepository reservationRepository, WaitingService waitingService) {
        this.reservationRepository = reservationRepository;
        this.waitingService = waitingService;
    }

    public List<UserReservationResponse> findAllMemberReservations(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);
        List<UserReservationResponse> reservationResponses = reservations.stream()
                .map(UserReservationResponse::from)
                .toList();
        List<WaitingWithRank> waitings = waitingService.findAllByMemberWithRank(memberId);
        List<UserReservationResponse> waitingResponses = waitings.stream()
                .map(UserReservationResponse::from)
                .toList();
        return mergeList(reservationResponses, waitingResponses);
    }

    private List<UserReservationResponse> mergeList(List<UserReservationResponse>... lists) {
        return Stream.of(lists)
                .flatMap(List::stream)
                .toList();
    }
}
