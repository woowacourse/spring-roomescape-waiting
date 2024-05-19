package roomescape;

import roomescape.domain.Email;
import roomescape.domain.LoginMember;
import roomescape.domain.Member;
import roomescape.domain.Name;
import roomescape.domain.Password;
import roomescape.domain.Role;

public class Fixture {
    public static final LoginMember defaultLoginuser = new LoginMember(1L,new Name("name"), Role.USER);
    public static final Member defaultMember =
            new Member(
                    defaultLoginuser,
                    new Email("email@email.com"),
                    new Password("password")
            );
}
