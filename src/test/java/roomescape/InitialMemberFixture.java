package roomescape;

import roomescape.member.domain.Email;
import roomescape.member.domain.LoginMember;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;

public class InitialMemberFixture {

    public static final int INITIAL_LOGIN_MEMBER_COUNT = 4;
    public static final LoginMember LOGIN_MEMBER_1 = new LoginMember(
            1L,
            new Name("카고"),
            new Email("kargo123@email.com"),
            Role.USER
    );
    public static final Password COMMON_PASSWORD = new Password("password");
    public static final LoginMember LOGIN_MEMBER_2 = new LoginMember(
            2L,
            new Name("브라운"),
            new Email("brown123@email.com"),
            Role.USER
    );
    public static final LoginMember LOGIN_MEMBER_3 = new LoginMember(
            3L,
            new Name("솔라"),
            new Email("solar123@email.com"),
            Role.USER
    );
    public static final LoginMember LOGIN_MEMBER_4 = new LoginMember(
            4L,
            new Name("어드민"),
            new Email("admin@email.com"),
            Role.ADMIN
    );

    public static final LoginMember NOT_SAVED_LOGIN_MEMBER = new LoginMember(
            new Name("네오"),
            new Email("neo123@email.com"),
            Role.USER
    );
}
