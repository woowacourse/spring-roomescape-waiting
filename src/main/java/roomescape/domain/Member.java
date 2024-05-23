package roomescape.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Member extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Embedded
    private Email email;
    private String password;
    @Enumerated(value = EnumType.STRING)
    private Role role;

    public Member(Long id,
                  String name,
                  String email,
                  String password,
                  Role role
    ) {
        validate(name, email, password, role);
        this.id = id;
        this.name = name;
        this.email = new Email(email);
        this.password = password;
        this.role = role;
    }

    public Member(String name,
                  String email,
                  String password,
                  Role role) {
        this(null, name, email, password, role);
    }

    protected Member() {
    }

    private void validate(String name,
                          String email,
                          String password,
                          Role role) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("사용자의 이름을 입력해주세요.");
        }
        if (email == null) {
            throw new IllegalArgumentException("사용자의 이메일을 입력해주세요.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("사용자의 비밀번호를 입력해주세요.");
        }
        if (role == null) {
            throw new IllegalArgumentException("사용자의 역할을 지정해주세요.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email.getEmail();
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Member member = (Member) object;
        return Objects.equals(id, member.id)
               && Objects.equals(name, member.name)
               && Objects.equals(email, member.email)
               && Objects.equals(password, member.password)
               && role == member.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, password, role);
    }
}
