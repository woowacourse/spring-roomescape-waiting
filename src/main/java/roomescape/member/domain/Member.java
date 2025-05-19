package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.regex.Pattern;
import roomescape.exception.DomainValidationException;

@Entity
@Table(name = "member")
public class Member {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(nullable = false)
    private String password;

    public Member() {
    }

    public Member(Long id, String name, String email, MemberRole role, String password) {
        validate(name, email, role, password);
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public Member(String name, String email, MemberRole role, String password) {
        validate(name, email, role, password);
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public static Member generateWithPrimaryKey(Member member, Long newPrimaryKey) {
        return new Member(newPrimaryKey, member.name, member.email, member.role, member.password);
    }

    public boolean checkInvalidLogin(String email, String password) {
        return !(this.email.equals(email) && this.password.equals(password));
    }

    private void validate(String name, String email, MemberRole role, String password) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("이름은 비어있을 수 없습니다.");
        }
        if (email == null || email.isBlank()) {
            throw new DomainValidationException("이메일은 비어있을 수 없습니다.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new DomainValidationException("유효하지 않은 이메일 형식입니다.");
        }
        if (role == null) {
            throw new DomainValidationException("유저 역할은 비어있을 수 없습니다.");
        }
        if (password == null || password.isBlank()) {
            throw new DomainValidationException("패스워드는 비어있을 수 없습니다.");
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

    public MemberRole getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        if (member.getId() == null || id == null) {
            return false;
        }
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
