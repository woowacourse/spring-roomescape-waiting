package roomescape.fixture;

import roomescape.domain.user.Member;
import roomescape.domain.user.Role;
import roomescape.service.dto.input.MemberCreateInput;

public class MemberFixture {
    public static MemberCreateInput getUserCreateInput() {
        return new MemberCreateInput("조이썬",
                "joyson5582@gmail.com",
                "password1234",
                Role.USER);
    }

    public static MemberCreateInput getAdminCreateInput() {
        return new MemberCreateInput("조이썬",
                "joyson5582@gmail.com",
                "password1234",
                Role.ADMIN);
    }

    public static Member getDomain() {
        return Member.fromMember(
                null,
                "조이썬",
                "joyson5582@gmail.com",
                "password1234"
        );
    }

    public static Member getDomain(final String email) {
        return Member.fromMember(
                null,
                "조이썬",
                email,
                "password1234"
        );
    }

}
