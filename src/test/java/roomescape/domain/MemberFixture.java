package roomescape.domain;

public class MemberFixture {
    public static Member defaultValue() {
        return of("test", "test@email.com", "wootecoCrew6!");
    }

    public static Member of(String name, String email, String password) {
        return new Member(new PlayerName(name), new Email(email), new Password(password), Role.BASIC);
    }
}
