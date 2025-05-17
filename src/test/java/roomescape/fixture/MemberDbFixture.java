package roomescape.fixture;

import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.infrastructure.repository.MemberRepository;

@Component
public class MemberDbFixture {

    private final MemberRepository memberRepository;

    public MemberDbFixture(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member 한스_사용자() {
        String name = "한스";
        Role role = Role.USER;
        String email = "test@test.com";
        String password = "pass1";

        return memberRepository.save(Member.create(name, role, email, password));
    }
}
