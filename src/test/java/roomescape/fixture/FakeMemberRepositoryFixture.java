package roomescape.fixture;

import java.util.List;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;
import roomescape.repository.FakeMemberRepository;

public class FakeMemberRepositoryFixture {

    public static FakeMemberRepository create() {
        return new FakeMemberRepository(List.of(
                new Member(1L, "어드민", new Email("admin@gmail.com"), new Password("wooteco7"), Role.ADMIN),
                new Member(2L, "회원", new Email("admin@gmail.com"), new Password("wooteco7"), Role.USER)
        ));
    }
}
