package roomescape.member.domain;

import jakarta.persistence.*;
import roomescape.exception.custom.BadRequestException;

import java.util.Objects;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private Name name;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public Member() {
    }

    public Member(Long id, String name, String email, String password, Role role) {
        validate(id, name, email, password, role);
        this.id = id;
        this.name = new Name(name);
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(String name, String email, String password, Role role) {
        this(null, name, email, password, role);
    }

    private void validate(Long id, String name, String email, String password, Role role) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("올바르지 않은 이름 입력 양식입니다.");
        }
        if (email == null || email.isBlank()) {
            throw new BadRequestException("올바르지 않은 이메일 입력 양식입니다.");
        }
        if (password == null || password.isBlank()) {// 필수 요청값이 누락되었습니다.
            throw new BadRequestException("필수 요청값이 누락되었습니다.");
        }
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return Objects.equals(getId(), member.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Member{" + "id=" + id + ", name=" + name + ", email='" + email + '\'' + '}';
    }
}
