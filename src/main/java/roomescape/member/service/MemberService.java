package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.dto.response.FindReservationResponse;
import roomescape.member.dto.response.FindWaitingRankResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.response.FindMembersResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public MemberService(MemberRepository memberRepository,
                         ReservationRepository reservationRepository,
                         WaitingRepository waitingRepository) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional(readOnly = true)
    public List<FindMembersResponse> getMembers() {
        return memberRepository.findAll().stream()
                .map(FindMembersResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FindReservationResponse> getReservationsByMember(AuthInfo authInfo) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(authInfo.getMemberId());
        return reservations.stream()
                .map(FindReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FindWaitingRankResponse> getWaitingsByMember(AuthInfo authInfo) {
        return waitingRepository.findAllWaitingResponses(authInfo.getMemberId());
    }
}
