package roomescape.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.regex.Pattern;
import roomescape.domain.Role;

@Entity
public class Member {

    private static final int NAME_MAX_LENGTH = 15;
    private static final int PASSWORD_MAX_LENGTH = 30;
    private static final int EMAIL_MAX_LENGTH = 50;
    private static final Pattern VALID_EMAIL_PATTERN = Pattern.compile("\\w+@\\w+\\.\\w+");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    private Member(Long id, String name, String email, String password, Role role) {
        validateMemberInfo(name, email, password);

        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    protected Member() {
    }

    public static Member withId(Long id, String name, String email, String password, Role role) {
        if (id == null) {
            throw new IllegalArgumentException("id를 입력해주세요.");
        }

        return new Member(id, name, email, password, role);
    }

    public static Member withoutId(String name, String email, String password, Role role) {
        return new Member(null, name, email, password, role);
    }

    private void validateMemberInfo(String name, String email, String password) {
        validateName(name);
        validateEmail(email);
        validatePassword(password);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.contains(" ")) {
            throw new IllegalArgumentException("이름은 공백이거나 공백을 포함할 수 없습니다.");
        }

        if (name.length() > NAME_MAX_LENGTH) {
            String message = String.format("이름은 %d자를 넘길 수 없습니다.", NAME_MAX_LENGTH);
            throw new IllegalArgumentException(message);
        }
    }

    private void validateEmail(String email) {
        if (!VALID_EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("잘못된 형식의 이메일입니다 : " + email);
        }

        if (email.length() > EMAIL_MAX_LENGTH) {
            String message = String.format("이메일은 %d자를 넘길 수 없습니다.", EMAIL_MAX_LENGTH);
            throw new IllegalArgumentException(message);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank() || password.contains(" ")) {
            throw new IllegalArgumentException("비밀번호는 공백이거나 공백을 포함할 수 없습니다.");
        }

        if (password.length() > PASSWORD_MAX_LENGTH) {
            String message = String.format("비밀번호는 %d자를 넘길 수 없습니다.", PASSWORD_MAX_LENGTH);
            throw new IllegalArgumentException(message);
        }
    }

    public Long getId() {
        return id;
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

    public Role getRole() {
        return role;
    }
}
