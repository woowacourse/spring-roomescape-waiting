package roomescape.member.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.regex.Pattern;

@Entity
@Table(name = "member")
public class Member {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
    public static final int MIN_PASSWORD_SIZE = 8;
    public static final int MAX_PASSWORD_SIZE = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
//    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    public Member() {
    }

    public Member(Long id, String name, String email, String password, Role role) {
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

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 사용자의 이름은 1글자 이상으로 이루어져야 합니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("[ERROR] 이메일이 없습니다.");
        }
        if (!Pattern.matches(EMAIL_REGEX, email)) {
            throw new IllegalArgumentException("[ERROR] 이메일 형식이 아닙니다.");
        }
    }

    private void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("[ERROR] 비밀번호가 없습니다.");
        }

        String trimmedPassword = password.trim();
        if (trimmedPassword.length() < MIN_PASSWORD_SIZE || trimmedPassword.length() > MAX_PASSWORD_SIZE) {
            throw new IllegalArgumentException("[ERROR] 비밀번호는 8자 이상, 50자 이하여야 합니다.");
        }
    }

    private void validateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("[ERROR] 사용자 권한을 명시해 주세요.");
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
