package roomescape.fixture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import roomescape.domain.user.Member;
import roomescape.domain.user.Role;
import roomescape.service.MemberService;
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
}
