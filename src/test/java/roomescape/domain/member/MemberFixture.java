package roomescape.domain.member;

public class MemberFixture {

    public static Member createMember(String name) {
        return new Member(
                null,
                new PlayerName(name),
                new Email("test@test.com"),
                new Password("12341234")
        );
    }
}
