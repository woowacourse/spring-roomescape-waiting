package roomescape.fixture;

import java.util.List;
import java.util.stream.IntStream;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public class MemberFixture {
    public static Member getOne() {
        return new Member("name", Role.USER, "email@naver.com", "password");
    }

    public static Member getOneWithId(final Long id) {
        return new Member(id, "name", Role.USER, "email@naver.com", "password");
    }

    public static Member getOne(final String email) {
        return new Member("name", Role.USER, email, "password");
    }

    public static List<Member> get(final int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new Member("name" + i, Role.USER, i + "email@naver.com", "password"))
                .toList();
    }
}
