package roomescape.member.business.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private MemberName name;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    public Member() {

    }

    public Member(final Long id, final MemberName name, final Email email, final Password password, final Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(final Long id, final String name, final String email, final String password, final Role role) {
        this(id, new MemberName(name), new Email(email), new Password(password), role);
    }

    public Member(final String name, final String email, final String password, final Role role) {
        this(null, new MemberName(name), new Email(email), new Password(password), role);
    }

    public Member(final String name, final String email, final String password) {
        this(null, new MemberName(name), new Email(email), new Password(password), Role.MEMBER);
    }

    public Long getId() {
        return id;
    }

    public MemberName getName() {
        return name;
    }

    public String getNameValue() {
        return name.getValue();
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
