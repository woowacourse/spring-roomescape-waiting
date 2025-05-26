package roomescape.member.domain;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import roomescape.member.repository.EmailConverter;
import roomescape.member.repository.PasswordConverter;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Convert(converter = EmailConverter.class)
    private Email email;

    @Convert(converter = PasswordConverter.class)
    private Password password;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    private Member() {
    }

    public Member(Long id, String name, Email email, Password password, Role role) {
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

    private void validateEmail(Email email) {
        if (email == null) {
            throw new IllegalArgumentException("[ERROR] 이메일이 없습니다.");
        }
    }

    private void validatePassword(Password password) {
        if (password == null) {
            throw new IllegalArgumentException("[ERROR] 비밀번호가 없습니다.");
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

    public Email getEmail() {
        return email;
    }

    public String getEmailValue() {
        return email.getValue();
    }

    public Password getPassword() {
        return password;
    }

    public String getPasswordValue() {
        return password.getValue();
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Member that)) return false;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }
}
