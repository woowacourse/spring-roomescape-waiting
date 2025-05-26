package roomescape.member.fixture;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public class MemberFixture {
    private static final AtomicLong identifier = new AtomicLong(1L);

    public static Member create(MemberRole role) {
        long id = identifier.getAndIncrement();
        return new Member(
            id,
            "testUser" + id,
            id + "testEmail@naver.com",
            role,
            "testPassword" + id
        );
    }

    public static Member createWithoutId(MemberRole role) {
        long id = identifier.getAndIncrement();
        return new Member(
            null,
            "testUser" + id,
            id + "testEmail@naver.com",
            role,
            "testPassword" + id
        );
    }

    public static List<Member> createMembers(int count, MemberRole role) {
        return IntStream.range(0, count)
            .mapToObj(i -> create(role))
            .toList();
    }
}
