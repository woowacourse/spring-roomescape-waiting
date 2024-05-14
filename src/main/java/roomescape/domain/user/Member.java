package roomescape.domain.user;

import java.util.Objects;

public class Member {
    private final Long id;
    private final Name name;
    private final Email email;
    private final Password password;
    private final Role role;

    public Member(final Long id, final Name name, final Email email, final Password password, final Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }
    public static Member fromMember(final Long id, final String name, final String email, final String password) {
        return new Member(id, new Name(name), new Email(email), new Password(password),Role.USER);
    }
    public static Member fromAdmin(final Long id, final String name, final String email, final String password) {
        return new Member(id, new Name(name), new Email(email), new Password(password),Role.ADMIN);
    }

    public static Member from(final Long id, final String name, final String email, final String password, final String role) {
        return new Member(id, new Name(name), new Email(email), new Password(password),Role.from(role));
    }
    public boolean isEqualId(final long id){
        return this.id.equals(id);
    }


    public boolean isNotEqualPassword(final String password) {
        return !this.password.isEqual(password);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final Member user)) return false;
        return Objects.equals(email, user.email) && Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }

    public String getName() {
        return name.name();
    }

    public String getEmail() {
        return email.value();
    }

    public String getPassword() {
        return password.value();
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role.getValue();
    }
}
