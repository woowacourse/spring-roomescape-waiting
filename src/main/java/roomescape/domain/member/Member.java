package roomescape.domain.member;

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

    private static final int PASSWORD_MAX_LENGTH = 255;
    private static final int NAME_MAX_LENGTH = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    protected Member() {
    }

    public Member(String email, String password, String name, Role role) {
        this(null, email, password, name, role);
    }

    private Member(Long id, String email, String password, String name, Role role) {
        validate(password, name, role);

        this.id = id;
        this.email = new Email(email);
        this.password = password;
        this.name = name;
        this.role = role;
    }

    private void validate(String password, String name, Role role) {
        validatePassword(password);
        validateName(name);
        validateRole(role);
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수 값입니다.");
        }

        if (password.length() > PASSWORD_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("비밀번호는 %d자를 넘을 수 없습니다.", PASSWORD_MAX_LENGTH));
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수 값입니다.");
        }

        if (name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("이름은 %d자를 넘을 수 없습니다.", NAME_MAX_LENGTH));
        }
    }

    private void validateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("역할은 필수 값입니다.");
        }
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

    public Long getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}
