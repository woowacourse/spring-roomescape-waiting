package roomescape.fixture.entity;

import roomescape.member.domain.Member;
import roomescape.member.domain.Password;

public class MemberFixture {

    public static final String USER_EMAIL = "user1@gmail.com";
    public static final String USER_NAME = "user1";
    public static final String USER_PASSWORD = "password";

    public Member createMember() {
        return Member.builder()
                .email(USER_EMAIL)
                .name(USER_NAME)
                .password(new Password(USER_PASSWORD))
                .build();
    }
}
