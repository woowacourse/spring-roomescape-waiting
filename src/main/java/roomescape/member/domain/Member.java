package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    @Column(name = "name", nullable = false)
    private MemberName name;
    @Embedded
    @Column(name = "email", nullable = false)
    private Email email;
    @Embedded
    @Column(name = "password", nullable = false)
    private Password password;
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public Member() {
    }

    public Member(final String name, final String email, final String password) {
        this(null, name, email, password, Role.MEMBER);
    }

    public Member(final String name, final String email, final String password, final Role role) {
        this(null, name, email, password, role);
    }

    public Member(final Long id, final Member member) {
        this(id, member.name, member.email, member.password, member.role);
    }

    public Member(final Long id, final String name, final String email, final String password, final Role role) {
        this(id, new MemberName(name), new Email(email), new Password(password), role);
    }

    public Member(final Long id, final MemberName name, final Email email, final Password password, final Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;

        validateRole();
    }

    private void validateRole() {
        if (role == null) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("회원(Member) 역할(Role)에 유효하지 않은 값(null OR 공백)이 입력되었습니다."));
        }
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean hasRole() {
        return role != null;
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

    public Role getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name=" + name +
                ", email=" + email +
                ", password=" + password +
                ", role=" + role +
                '}';
    }
}
