package roomescape.member.domain;


import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.auth.domain.AuthRole;

@Getter
@EqualsAndHashCode(of = {"id"})
public class Member {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final AuthRole role;

    public Member(final Long id, final String name, final String email, final String password, final AuthRole role) {
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

    public Member(final String name, final String email, final String password, final AuthRole role) {
        this(null, name, email, password, role);
    }

    private void validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 null 이거나 빈 문자열일 수 없습니다.");
        }
    }

    private void validateEmail(final String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 null 이거나 빈 문자열일 수 없습니다.");
        }
        if (!Pattern.matches(EMAIL_REGEX, email)) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
    }

    private void validatePassword(final String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 null 이거나 빈 문자열일 수 없습니다.");
        }
    }

    private void validateRole(final AuthRole role) {
        if (role == null) {
            throw new IllegalArgumentException("역할은 null 일 수 없습니다.");
        }
    }

    public boolean isWrongPassword(final String password) {
        return !this.password.equals(password);
    }

    public boolean isAdmin() {
        return role == AuthRole.ADMIN;
    }

    public String getRoleName() {
        return role.getRoleName();
    }


}
