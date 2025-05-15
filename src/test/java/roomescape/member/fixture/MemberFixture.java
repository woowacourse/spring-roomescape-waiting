package roomescape.member.fixture;

import java.util.List;
import java.util.stream.IntStream;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public class MemberFixture {
    private static Long identifier = 1L;

    public static Member createMember(MemberRole role) {
        identifier++;
        return new Member(
            "testUser" + identifier,
            identifier + "testEmail@naver.com",
            role,
            "testPassword" + identifier
        );
    }

    public static List<Member> createMembers(int count, MemberRole role) {
        return IntStream.range(0, count)
            .mapToObj(i -> createMember(role))
            .toList();
    }
}
