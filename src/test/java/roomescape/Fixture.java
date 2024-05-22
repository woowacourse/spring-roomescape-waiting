package roomescape;

import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.Name;
import roomescape.domain.Password;
import roomescape.domain.Role;
import roomescape.dto.LoginMemberRequest;

public class Fixture {
    public static final Member defaultMember =
            new Member(
                    1L,
                    new Name("name"),
                    Role.USER,
                    new Email("email@email.com"),
                    new Password("password")
            );
    public static final LoginMemberRequest defaultLoginuser = new LoginMemberRequest(
            defaultMember.getId(),
            defaultMember.getName(),
            defaultMember.getRole()
    );
}
