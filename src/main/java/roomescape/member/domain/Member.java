package roomescape.member.domain;

import java.util.regex.Pattern;
import lombok.Getter;
import roomescape.common.exception.MemberException;

@Getter
public class Member {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;

    public Member(final Long id, final String name, final String email, final String password, final Role role) {
        validateName(name);
        validateEmail(email);
        validateEmailFormat(email);
        validatePassword(password);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(Long id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = null;
    }

    private void validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new MemberException("Name cannot be null or blank");
        }
    }

    private void validateEmail(final String email) {
        if (email == null || email.isBlank()) {
            throw new MemberException("Email cannot be null or blank");
        }
    }

    private void validatePassword(final String password) {
        if (password == null || password.isBlank()) {
            throw new MemberException("Password cannot be null or blank");
        }
    }

    private void validateEmailFormat(final String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new MemberException("Invalid email format");
        }
    }
}
