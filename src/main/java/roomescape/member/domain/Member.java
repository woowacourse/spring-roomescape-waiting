package roomescape.member.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.common.exception.MemberException;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

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

    public Member(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(final Long id, final String name, final String email, final Role role) {
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
