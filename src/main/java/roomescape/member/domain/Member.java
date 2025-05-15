package roomescape.member.domain;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    private MemberName name;
    @Embedded @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false))
    private Email email;
    @Embedded @AttributeOverride(name = "value", column = @Column(name = "password", nullable = false))
    private Password password;
    @Enumerated(value = EnumType.STRING)
    private Role role;

    protected Member() {}

    public Member(final Long id, final String name, final String email, final String password, final Role role) {
        this.id = id;
        this.name = new MemberName(name);
        this.email = new Email(email);
        this.password = new Password(password);
        this.role = role;
    }

    public Member(final String name, final String email, final String password, final Role role) {
        this(null, name, email, password, role);
    }

    public Long getId() {
        return id;
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

    public String getRole() {
        return role.name();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Member member)) {
            return false;
        }
        return Objects.equals(getId(), member.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
