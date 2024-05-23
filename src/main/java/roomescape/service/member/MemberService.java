package roomescape.service.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationWithRank;
import roomescape.exception.InvalidMemberException;
import roomescape.service.member.dto.MemberReservationResponse;
import roomescape.service.member.dto.MemberResponse;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public MemberService(MemberRepository memberRepository, ReservationRepository reservationRepository) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::new)
                .toList();
    }

    public Member findById(long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 회원입니다."));
    }

    public List<MemberReservationResponse> findReservations(long memberId) {
        List<ReservationWithRank> reservationWithRanks = reservationRepository.findWithRankingByMemberId(memberId);
        return reservationWithRanks.stream()
                .map(MemberReservationResponse::from)
                .toList();
    }
}
