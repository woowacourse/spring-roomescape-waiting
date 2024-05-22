package roomescape;

import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;

public class InitialMemberFixture {

    public static final Password COMMON_PASSWORD = new Password("password");
    public static final int INITIAL_LOGIN_MEMBER_COUNT = 5;
    public static final Member MEMBER_1 = new Member(
            1L,
            new Name("카고"),
            new Email("kargo123@email.com"),
            Role.USER,
            COMMON_PASSWORD
    );
    public static final Member MEMBER_2 = new Member(
            2L,
            new Name("브라운"),
            new Email("brown123@email.com"),
            Role.USER,
            COMMON_PASSWORD
    );
    public static final Member MEMBER_3 = new Member(
            3L,
            new Name("솔라"),
            new Email("solar123@email.com"),
            Role.USER,
            COMMON_PASSWORD
    );
    public static final Member MEMBER_4 = new Member(
            4L,
            new Name("어드민"),
            new Email("admin@email.com"),
            Role.ADMIN,
            COMMON_PASSWORD
    );

    public static final Member NO_RESERVATION_MEMBER = new Member(
            5L,
            new Name("파랑"),
            new Email("blue123@email.com"),
            Role.USER,
            COMMON_PASSWORD
    );

    public static final Member NOT_SAVED_MEMBER = new Member(
            6L,
            new Name("네오"),
            new Email("neo123@email.com"),
            Role.USER,
            COMMON_PASSWORD
    );
}
