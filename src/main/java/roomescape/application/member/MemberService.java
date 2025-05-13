package roomescape.application.member;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberResult> findAll() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(MemberResult::from)
                .toList();
    }
}
