package roomescape.domain.member;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.exception.BadRequestException;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;

    @Embedded
    private MemberPassword password;
    private String name;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    public Member(String email, MemberPassword password, String name, Role role) {
        validateEmail(email);
        this.email = email;
        this.password = password;
        validateName(name);
        this.name = name;
        this.role = role;
    }

    public Member(String email, String password, String name, String role) {
        this(email, new MemberPassword(password), name, Role.getRole(role));
    }

    public Member(String email, MemberPassword password, String name) {
        this(email, password, name, Role.MEMBER);
    }

    protected Member() {
    }

    public boolean isMismatchedPassword(MemberPassword other) {
        return this.password.isMismatchedPassword(other);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("이메일은 반드시 입력되어야 합니다.");
        }
        if (email.length() > 30) {
            throw new BadRequestException("이메일 길이는 30글자까지 가능합니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("이름은 반드시 입력되어야 합니다.");
        }
        if (name.length() > 15) {
            throw new BadRequestException("이름 길이는 15글자까지 가능합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public String password() {
        return password.getPassword();
    }
}
