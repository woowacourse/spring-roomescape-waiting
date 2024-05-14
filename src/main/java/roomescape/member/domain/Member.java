package roomescape.member.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Member {

    @Id
    @GeneratedValue
    private Long id;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "member_name", nullable = false))
    private MemberName memberName;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false))
    private Email email;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "password", nullable = false))
    private Password password;
    @Enumerated(EnumType.STRING)
    private Role role;

    public Member(
            final Long id, final MemberName memberName, final Email email,
            final Password password, final Role role
    ) {
        this.id = id;
        this.memberName = memberName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member() {
    }

    public static Member of(final String name, final String email, final String password, final String role) {
        return new Member(null, new MemberName(name), new Email(email), new Password(password), Role.from(role));
    }

    public Long getId() {
        return id;
    }

    public String getNameValue() {
        return memberName.getValue();
    }

    public String getEmail() {
        return email.getValue();
    }

    public String getPassword() {
        return password.getValue();
    }

    public Role getRole() {
        return role;
    }
}
