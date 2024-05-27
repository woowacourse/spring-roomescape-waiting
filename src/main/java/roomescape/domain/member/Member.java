package roomescape.domain.member;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import roomescape.exception.UnauthorizedException;

@Entity
@Table(name = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private MemberName memberName;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @Enumerated(EnumType.STRING)
    private Role role;

    protected Member() {
    }

    public Member(MemberName memberName, Email email, Password password, Role role) {
        this.memberName = memberName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(String name, String email, String password, Role role) {
        this(new MemberName(name), Email.of(email), Password.of(password), role);
    }

    public void validatePassword(Password other) {
        if (!password.equals(other)) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 잘못되었습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public MemberName getMemberName() {
        return memberName;
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
}
