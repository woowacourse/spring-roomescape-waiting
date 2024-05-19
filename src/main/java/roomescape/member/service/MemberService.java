package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberIdNameResponse;
import roomescape.member.dto.MemberNameResponse;
import roomescape.member.dto.MemberRequest;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository MemberRepository) {
        this.memberRepository = MemberRepository;
    }

    public List<MemberIdNameResponse> findMembersIdAndName() {
        return memberRepository.findAll()
                .stream()
                .map(MemberIdNameResponse::new)
                .toList();
    }

    public boolean isAdmin(MemberRequest memberRequest) {
        Member member = memberRequest.toMember();

        return member.isAdmin();
    }

    public MemberNameResponse getMemberName(MemberRequest memberRequest) {
        Member member = memberRequest.toMember();
        return new MemberNameResponse(member);
    }
}
