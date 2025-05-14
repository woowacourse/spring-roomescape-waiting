package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

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
    @OneToMany(mappedBy = "user")
    private List<Reservation> reservations;

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
        if (name.isBlank() || name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("이름은 공백이거나 %d자를 넘길 수 없습니다.", NAME_MAX_LENGTH));
        }
    }

    private void validateEmailFormat(final String email) {
        if (email.matches(VALID_EMAIL_FORMAT)) {
            return;
        }
        throw new IllegalArgumentException("잘못된 형식의 이메일입니다 : " + email);
    }

    private void validatePasswordLength(final String password) {
        if (password.isBlank() || password.length() > PASSWORD_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("비밀번호는 공백이거나 %d자를 넘길 수 없습니다.", PASSWORD_MAX_LENGTH));
        }
    }
}
