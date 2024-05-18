package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Embedded
    @Column(nullable = false)
    private Name name;

    @NotNull
    @Embedded
    @Column(nullable = false)
    private Email email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @NotNull
    @Embedded
    @Column(nullable = false)
    private Password password;

    protected Member() {
    }

    public Member(Long id, Name name, Email email, Role role, Password password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public Member(Name name, Email email, Role role, Password password) {
        this(null, name, email, role, password);
    }

    public Member(Long id, String name, String email, String role, String password) {
        this(id, new Name(name), new Email(email), Role.valueOf(role), new Password(password));
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isPassword(Password password) {
        return Objects.equals(this.password, password);
    }

    public Long getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public Password getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member that = (Member) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
