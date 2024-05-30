package roomescape.member.domain;

import java.util.Objects;

import jakarta.persistence.Column;
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
    @Column(name = "id")
    private Long id;
    @Column(nullable = false, name = "name")
    private String name;
    @Column(nullable = false, unique = true, name = "email")
    private String email;
    @Column(nullable = false, name = "password")
    private String password;
    @Column(nullable = false, name = "role")
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    protected Member() {
    }

    public Member(String name, String email, String password) {
        this(null, name, email, password, MemberRole.USER);
    }

    public Member(Long id, String name, String email, String password) {
        this(id, name, email, password, MemberRole.USER);
    }

    public Member(Long id, String name, String email, String password, MemberRole role) {
        validateNotNull(name, email, password, role);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    private void validateNotNull(String name, String email, String password, MemberRole role) {
        try {
            Objects.requireNonNull(name, "이름이 입력되지 않았습니다.");
            Objects.requireNonNull(email, "이메일이 입력되지 않았습니다.");
            Objects.requireNonNull(password, "비밀번호가 입력되지 않았습니다.");
            Objects.requireNonNull(role, "사용자의 권한이 부여되지 않았습니다.");
        } catch (NullPointerException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public boolean isAdmin() {
        return role.isAdmin();
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

    public MemberRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member member)) return false;

        if (id == null || member.id == null) {
            return Objects.equals(email, member.email);
        }
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        if (id == null) return Objects.hash(email);
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Member{" +
               "id=" + id +
               ", email='" + email + '\'' +
               '}';
    }
}
