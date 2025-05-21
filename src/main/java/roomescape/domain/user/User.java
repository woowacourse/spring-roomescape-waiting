package roomescape.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.InvalidInputException;

@EqualsAndHashCode(of = {"id"})
@Getter
@Accessors(fluent = true)
@ToString
@Entity(name = "USERS")
public class User {

    private static final int NAME_MAX_LENGTH = 5;
    private static final int PASSWORD_MAX_LENGTH = 30;
    private static final String VALID_EMAIL_FORMAT = "\\w+@\\w+\\.\\w+";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private String email;
    private String password;


    public User(final Long id, final String name, final UserRole role, final String email, final String password) {
        validateNameLength(name);
        validateEmailFormat(email);
        validatePasswordLength(password);
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    protected User() {
    }

    public static User createUser(final String name, final String email, final String password) {
        return new User(null, name, UserRole.USER, email, password);
    }

    public boolean matchesPassword(final String passwordToCompare) {
        return password.equals(passwordToCompare);
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    private void validateNameLength(final String name) {
        if (name.isBlank()) {
            throw new InvalidInputException("이름은 공백일 수 없습니다.");
        }

        if (name.length() > NAME_MAX_LENGTH) {
            throw new BusinessRuleViolationException(String.format("이름은 %d자를 넘길 수 없습니다.", NAME_MAX_LENGTH));
        }
    }

    private void validateEmailFormat(final String email) {
        if (!email.matches(VALID_EMAIL_FORMAT)) {
            throw new InvalidInputException("잘못된 형식의 이메일입니다 : " + email);
        }
    }

    private void validatePasswordLength(final String password) {
        if (password.isBlank()) {
            throw new InvalidInputException("비밀번호는 공백일 수 없습니다.");
        }

        if (password.length() > PASSWORD_MAX_LENGTH) {
            throw new BusinessRuleViolationException(String.format("비밀번호는 %d자를 넘길 수 없습니다.", PASSWORD_MAX_LENGTH));
        }
    }
}
