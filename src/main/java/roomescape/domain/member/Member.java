package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class Member {

    private static final int MAX_NAME_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String password;

    public Member(Long id, String name, String email, Role role, String password) {
        this.id = id;
        this.name = ensureNameIsValid(name);
        this.email = ensureEmailIsValid(email);
        this.role = Objects.requireNonNull(role, "역할이 존재해야 합니다.");
        this.password = ensurePasswordIsValid(password);
    }

    protected Member() {
    }

    public Member (String name, String email, Role role, String password) {
        this(null, name, email, role, password);
    }

    private String ensureNameIsValid(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 1글자 이상으로 이루어져야 합니다. ");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("이름은 255자를 초과할 수 없습니다. 이름 길이 : " + name.length());
        }
        return name;
    }

    private String ensureEmailIsValid(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 1글자 이상으로 이루어져야 합니다. ");
        }
        if (email.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("이메일은 255자를 초과할 수 없습니다. 이메일 길이 : " + email.length());
        }
        return email;
    }

    private String ensurePasswordIsValid(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 1글자 이상으로 이루어져야 합니다. ");
        }
        if (password.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 255자를 초과할 수 없습니다. 비밀번호 길이 : " + password.length());
        }
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return Objects.equals(id, member.id) && Objects.equals(name, member.name) && Objects.equals(email, member.email)
                && Objects.equals(role, member.role) && Objects.equals(password, member.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, role, password);
    }

    public boolean isSameIdWith(long id) {
        return this.id.equals(id);
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

    public String getPassword() {
        return password;
    }
}
