package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.repository.MemberRepository;
import roomescape.service.response.MemberResponse;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberResponse> findAllMembers() {
        List<Member> members = memberRepository.findAll();
        return MemberResponse.from(members);
    }
}
