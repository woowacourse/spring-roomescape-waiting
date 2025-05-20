package roomescape.fixture.entity;

import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;

public class MemberFixture {

    public static final String ADMIN_EMAIL = "admin1@gmail.com";
    public static final String USER_EMAIL = "user1@gmail.com";
    public static final String USER_NAME = "user1";
    public static final String USER_PASSWORD = "password";

    public static Member createUser() {
        return Member.builder()
                .email(USER_EMAIL)
                .name(USER_NAME)
                .password(new Password(USER_PASSWORD))
                .role(Role.USER)
                .build();
    }

    public static Member createAdmin() {
        return Member.builder()
                .email(ADMIN_EMAIL)
                .name(USER_NAME)
                .password(new Password(USER_PASSWORD))
                .role(Role.ADMIN)
                .build();
    }
}
