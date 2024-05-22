package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.dto.response.FindReservationResponse;
import roomescape.member.dto.response.FindWaitingResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.response.FindMembersResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.Waiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public MemberService(final MemberRepository memberRepository, final ReservationRepository reservationRepository,
                         WaitingRepository waitingRepository) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<FindMembersResponse> getMembers() {
        return memberRepository.findAll().stream()
                .map(FindMembersResponse::of)
                .toList();
    }


    public List<FindReservationResponse> getReservationsByMember(final AuthInfo authInfo) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(authInfo.getMemberId());
        return reservations.stream()
                .map(FindReservationResponse::from)
                .toList();
    }

    public List<FindWaitingResponse> getWaitingsByMember(AuthInfo authInfo) {
        List<Waiting> waitings = waitingRepository.findAllByMemberId(authInfo.getMemberId());
        return waitings.stream()
                .map(FindWaitingResponse::from)
                .toList();
    }
}
