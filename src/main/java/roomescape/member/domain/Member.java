package roomescape.member.domain;

import jakarta.persistence.*;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

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
            throw new BusinessException(ErrorType.NAME_FORMAT_ERROR);
        }
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorType.EMAIL_FORMAT_ERROR);
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorType.MISSING_REQUIRED_VALUE_ERROR);
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
