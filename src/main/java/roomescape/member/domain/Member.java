package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.member.role.MemberRole;
import roomescape.name.domain.Name;

@Entity
public class Member {

    private static final String DEFAULT_NAME = "어드민";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Embedded
    private Name name;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    public Member() {
    }

    private Member(long id, String name, Email email, Password password, MemberRole role) {
        this.id = id;
        this.name = new Name(name);
        this.email = email;
        this.password = password;
        this.role = role;
    }

    private Member(String name, Email email, Password password, MemberRole role) {
        this(0, name, email, password, role);
    }

    public static Member saveMemberFrom(long id) {
        return new Member(id, DEFAULT_NAME, Email.saveEmailFrom(), Password.savePasswordFrom(),
                MemberRole.MEMBER);
    }

    public static Member memberOf(long id, String name, String email, String password, String role) {
        return new Member(id, name, Email.emailFrom(email), Password.passwordFrom(password),
                MemberRole.findMemberRole(role));
    }

    public static Member saveMemberOf(String email, String password) {
        return new Member(DEFAULT_NAME, Email.emailFrom(email), Password.passwordFrom(password), MemberRole.MEMBER);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getEmail() {
        return email.getEmail();
    }

    public String getPassword() {
        return password.getPassword();
    }

    public MemberRole getRole() {
        return role;
    }
}
