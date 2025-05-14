package roomescape.application.member;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.BusinessRuleViolationException;
import roomescape.domain.member.Email;
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

    @Transactional
    public void register(RegisterParam serviceParam) {
        if (memberRepository.existsByEmail(new Email(serviceParam.email()))) {
            throw new BusinessRuleViolationException("이미 같은 이메일을 가진 사용자가 존재합니다.");
        }
        Member member = new Member(
                serviceParam.name(),
                new Email(serviceParam.email()),
                serviceParam.password()
        );
        memberRepository.save(member);
    }
}
