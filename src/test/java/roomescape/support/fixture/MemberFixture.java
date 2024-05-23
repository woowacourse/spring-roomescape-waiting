package roomescape.support.fixture;

import org.springframework.boot.test.context.TestComponent;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

@TestComponent
public class MemberFixture extends Fixture {

    public Member save() {
        return save("prin@email.com");
    }

    public Member save(String email) {
        Member member = new Member(email, "password", "프린", Role.USER);
        em.persist(member);
        synchronize();
        return member;
    }
}
