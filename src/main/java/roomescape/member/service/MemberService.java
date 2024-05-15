package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.domain.Member;
import roomescape.member.dto.response.FindReservationResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.response.FindMembersResponse;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<FindMembersResponse> getMembers() {
        return memberRepository.findAll().stream()
                .map(FindMembersResponse::of)
                .toList();
    }

    public List<FindReservationResponse> getReservationsByMember(final AuthInfo authInfo) {
        Long memberId = authInfo.getMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("식별자 " + memberId + "에 해당하는 회원이 존재하지 않아 회원의 예약을 조회할 수 없습니다."));
        return member.getReservations().stream()
                        .map(FindReservationResponse::from)
                        .toList();
    }
}
