package roomescape.domain.user;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private Name name;
    @Embedded
    private Email email;
    @Embedded
    private Password password;
    @Enumerated(EnumType.STRING)
    private Role role;

    protected Member() {
    }

    public Member(final Name name, final Email email, final Password password, final Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static Member from(final String name, final String email, final String password, final Role role) {
        return new Member(new Name(name), new Email(email), new Password(password), role);
    }

    public boolean isNotEqualPassword(final String password) {
        return !this.password.isEqual(password);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final Member member)) return false;
        return Objects.equals(email, member.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }

    public String getName() {
        return name.name();
    }

    public String getEmail() {
        return email.address();
    }

    public String getPassword() {
        return password.password();
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role.getValue();
    }
}
