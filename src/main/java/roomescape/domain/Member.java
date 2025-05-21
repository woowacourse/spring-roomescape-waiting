package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import roomescape.domain.regex.MemberFormat;

@Entity
public class Member {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MAX_PASSWORD_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;

    protected Member() {
    }

    public Member(Long id, Role role, String name, String email, String password) {
        validate(role, name, email, password);
        this.id = id;
        this.role = role;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public static Member createWithoutId(Role role, String name, String email, String password) {
        return new Member(null, role, name, email, password);
    }

    public boolean isEqualPassword(String password) {
        return this.password.equals(password);
    }

    public boolean isMember() {
        return this.role.equals(Role.GENERAL);
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        if (this.id == null || member.id == null) {
            return false;
        }
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    private void validate(Role role, String name, String email, String password) {
        validateRole(role);
        validateName(name);
        validateEmail(email);
        validatePassword(password);
    }

    private void validateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("비어있는 권한으로 멤버를 생성할 수 없습니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("비어있는 이름로 멤버를 생성할 수 없습니다.");
        }
        if (name.length() >= MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("최대길이를 초과한 이름으로는 멤버를 생성할 수 없습니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("비어있는 이메일로 멤버를 생성할 수 없습니다.");
        }
        if (email.length() >= MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("최대길이를 초과한 이메일로는 멤버를 생성할 수 없습니다.");
        }
        if (!email.matches(MemberFormat.EMAIL)) {
            throw new IllegalArgumentException("올바르지 않은 형식의 이메일로는 멤버를 생성할 수 없습니다.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비어있는 비밀번호로 멤버를 생성할 수 없습니다.");
        }
        if (password.length() >= MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("최대길이를 초과한 비밀번호로는 멤버를 생성할 수 없습니다.");
        }
        if (!password.matches(MemberFormat.PASSWORD)) {
            throw new IllegalArgumentException("올바르지 않은 형식의 비밀번호로는 멤버를 생성할 수 없습니다.");
        }
    }
}
