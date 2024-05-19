package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import roomescape.exception.BadRequestException;

@Entity
public class Member {

    private static final String EMAIL_PATTERN = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    @Enumerated(value = EnumType.STRING)
    private Role role;

    protected Member() {
    }

    public Member(String name, String email, String password, Role role) {
        this(null, name, email, password, role);
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

    private void validateRole(Role role) {
        if (role == null) {
            throw new BadRequestException("역할에 빈값을 입력할 수 없습니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("이름에 빈값을 입력할 수 없습니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("이메일에 빈값을 입력할 수 없습니다.");
        }

        if (!email.matches(EMAIL_PATTERN)) {
            throw new BadRequestException("이메일 형식이 올바르지 않습니다.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("비밀번호에 빈값을 입력할 수 없습니다.");
        }
    }

    public Member setId(Long id) {
        return new Member(id, name, email, password, role);
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

    public Role getRole() {
        return role;
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
}
