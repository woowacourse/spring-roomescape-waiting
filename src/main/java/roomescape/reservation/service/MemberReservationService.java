package roomescape.reservation.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingRepository;

import static java.util.Comparator.comparing;

@Service
@Transactional(readOnly = true)
public class MemberReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public MemberReservationService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<MemberReservationResponse> findAll(Long memberId) {
        List<MemberReservationResponse> allMemberReservations = findAllMemberReservations(memberId);
        List<MemberReservationResponse> allMemberWaitings = findAllMemberWaitings(memberId);

        return Stream.of(allMemberReservations, allMemberWaitings)
                .flatMap(Collection::stream)
                .sorted(comparing(MemberReservationResponse::date)
                        .thenComparing(MemberReservationResponse::time))
                .toList();
    }

    private List<MemberReservationResponse> findAllMemberReservations(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);
        return reservations.stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    private List<MemberReservationResponse> findAllMemberWaitings(Long memberId) {
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingWithRankByMemberId(memberId);
        return waitingWithRanks.stream()
                .map(MemberReservationResponse::from)
                .toList();
    }
}
