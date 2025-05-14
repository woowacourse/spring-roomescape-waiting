package roomescape.fixture;

import org.springframework.stereotype.Component;
import roomescape.member.domain.Member;
import roomescape.member.service.out.MemberRepository;

@Component
public class MemberDbFixture {

    private final MemberRepository memberRepository;

    public MemberDbFixture(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member 유저1_생성() {
        Member member = Member.signUpUser("유저1", "user1@email.com", "1234");
        return memberRepository.save(member);
    }

    public Member 관리자_생성() {
        Member member = Member.signUpAdmin("관리자", "admin@email.com", "1234");
        return memberRepository.save(member);
    }
}
