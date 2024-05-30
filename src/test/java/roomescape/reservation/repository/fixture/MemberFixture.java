package roomescape.reservation.repository.fixture;

import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;

public enum MemberFixture {

    MEMBER1(1L, "제리", "jerry@gmail.com", "password", Role.ADMIN),
    MEMBER2(2L, "오리", "duck@gmail.com", "password", Role.MEMBER),
    MEMBER3(3L, "안나", "anna@gmail.com", "password", Role.MEMBER),
    ;

    private final long id;
    private final String memberName;
    private final String email;
    private final String password;
    private final Role role;

    MemberFixture(long id, String memberName, String email, String password, Role role) {
        this.id = id;
        this.memberName = memberName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static int count() {
        return values().length;
    }

    public Member create() {
        return new Member(
                id, new MemberName(memberName), new Email(email), new Password(password), role
        );
    }
}
