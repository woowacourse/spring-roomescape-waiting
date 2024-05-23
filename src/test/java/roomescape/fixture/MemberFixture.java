package roomescape.fixture;

import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.Password;
import roomescape.domain.member.PlayerName;
import roomescape.domain.member.Role;

public enum MemberFixture {

    MEMBER_ARU("아루", "member@test.com", "12341234", Role.MEMBER),
    MEMBER_PK("피케이", "pk@test.com", "12341234", Role.MEMBER),
    ADMIN_PK("피케이", "admin@test.com", "12341234", Role.ADMIN),
    ;

    private final String name;
    private final String email;
    private final String password;
    private final Role role;

    MemberFixture(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member create() {
        return createWithId(null);
    }

    public Member createWithId(Long id) {
        return new Member(
                id,
                new PlayerName(name),
                new Email(email),
                new Password(password),
                role
        );
    }

    public static Member createMember(String name) {
        return new Member(
                new PlayerName(name),
                new Email(name + "@test.com"),
                new Password("12341234")
        );
    }

    public String email() {
        return email;
    }
}
