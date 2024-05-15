package roomescape.domain.user;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private Name name;
    @Embedded
    private Email email;
    @Embedded
    private Password password;
    private Role role;

    public Member() {
    }

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
        return email.email();
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
