package roomescape.domain.member;

public class Member {
    private final Long id;
    private final PlayerName name;
    private final Email email;
    private final Password password;

    public Member(Long id, PlayerName name, Email email, Password password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Member(String name, String email, String password) {
        this(null, new PlayerName(name), new Email(email), new Password(password));
    }

    public Member withId(long id) {
        return new Member(id, name, email, password);
    }

    public boolean hasId(long memberId) {
        return id.equals(memberId);
    }

    public boolean matchPassword(String otherPassword) {
        return password.matches(otherPassword);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getEmail() {
        return email.getAddress();
    }

    public String getPassword() {
        return password.getPassword();
    }
}
