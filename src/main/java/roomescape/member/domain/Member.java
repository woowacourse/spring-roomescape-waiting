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

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Embedded
    private Email email;

    @Column(nullable = false)
    private String password;

    public Member(String name, Role role, String email, String password) {
        this(null, name, role, email, password);
    }

    public Member(Long id, String name, Role role, String email, String password) {
        validateNameIsNull(name);
        validateRoleIsNull(role);
        validatePasswordIsNull(password);

        this.id = id;
        this.name = name;
        this.role = role;
        this.email = new Email(email);
        this.password = password;
    }

    protected Member() {
    }

    private void validateNameIsNull(String name) {
        if (name == null) {
            throw new IllegalArgumentException("회원 생성 시 이름은 필수입니다.");
        }
    }

    private void validateRoleIsNull(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("회원 생성 시 회원 권한 지정은 필수입니다.");
        }
    }

    private void validatePasswordIsNull(String password) {
        if (password == null) {
            throw new IllegalArgumentException("회원 생성 시 비밀번호는 필수입니다.");
        }
    }

    public boolean hasNotSamePassword(String password) {
        return !this.password.equals(password);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Role getMemberRole() {
        return role;
    }

    public Email getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
