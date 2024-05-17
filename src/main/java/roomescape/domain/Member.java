package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @NotNull
    private MemberName name;

    @Embedded
    private MemberEmail email;

    @Embedded
    private MemberPassword password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    public Member() {
    }

    public Member(MemberName name, MemberEmail email, MemberPassword password, MemberRole role) {
        this(null, name, email, password, role);
    }

    public Member(Long id, MemberName name, MemberEmail email, MemberPassword password, MemberRole role) {
        validateName(name);
        validateEmail(email);
        validatePassword(password);
        validateRole(role);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static Member createUser(MemberName name, MemberEmail email, MemberPassword password) {
        return new Member(name, email, password, MemberRole.USER);
    }

    private void validateName(MemberName name) {
        if (name == null) {
            throw new IllegalArgumentException("사용자 이름은 필수입니다.");
        }
    }

    private void validateEmail(MemberEmail email) {
        if (email == null) {
            throw new IllegalArgumentException("사용자 이메일은 필수입니다.");
        }
    }

    private void validatePassword(MemberPassword password) {
        if (password == null) {
            throw new IllegalArgumentException("사용자 비밀 번호는 필수입니다.");
        }
    }

    private void validateRole(MemberRole role) {
        if (role == null) {
            throw new IllegalArgumentException("사용자 권한은 필수입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public MemberName getName() {
        return name;
    }

    public MemberEmail getEmail() {
        return email;
    }

    public MemberPassword getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }
}
