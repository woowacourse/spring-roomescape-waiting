package roomescape.fixture;

import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.infrastructure.BcryptPasswordEncoder;

public class MemberFixture {

    public static Member createMember(String name, String email, String password) {
        return new Member(new Name(name), new Email(email), new Password(password, new BcryptPasswordEncoder()));
    }
}
