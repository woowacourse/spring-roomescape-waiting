package roomescape.domain.member;

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
    private static final long NO_ID = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name"))
    private MemberName memberName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email"))
    private Email email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "password"))
    private Password password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public Member() {
    }

    public Member(long id, MemberName memberName, Email email, Password password, Role role) {
        this.id = id;
        this.memberName = memberName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(String name, String email, String password, Role role) {
        this(NO_ID, new MemberName(name), new Email(email), new Password(password), role);
    }

    public Member(long id, Member member) {
        this(id, member.memberName, member.email, member.password, member.role);
    }

    public Member(long id, String name, String email, String password, String role) {
        this(id, new MemberName(name), new Email(email), new Password(password), Role.valueOf(role));
    }

    public long getId() {
        return id;
    }

    public String getMemberName() {
        return memberName.getValue();
    }

    public String getEmail() {
        return email.getValue();
    }

    public String getPassword() {
        return password.getValue();
    }

    public String getRole() {
        return role.name();
    }
}
