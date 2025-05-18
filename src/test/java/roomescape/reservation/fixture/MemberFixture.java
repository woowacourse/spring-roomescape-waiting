package roomescape.reservation.fixture;

import static roomescape.member.role.Role.ADMIN;

import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;

public class MemberFixture {

    public static final Member MATT = new Member(
            1L, new Name("매트"), new Email("matt.kakao"), new Password("1234"), ADMIN
    );
}
