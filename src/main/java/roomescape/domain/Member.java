package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import roomescape.domain.enums.Role;
import roomescape.exception.member.MemberFieldRequiredException;

@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    protected Member() {
    }

    public Member(Long id, String name, String email, String password, Role role) {
        validate(name, email, password, role);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public boolean isAdmin() {
        return role.equals(Role.ADMIN);
    }

    private void validate(String name, String email, String password, Role role) {
        validateName(name);
        validateEmail(email);
        validatePassword(password);
        validateRole(role);
    }

    private void validateRole(Role role) {
        if (role == null) {
            throw new MemberFieldRequiredException("역할");
        }
    }

    private void validatePassword(String password) {
        if (password == null) {
            throw new MemberFieldRequiredException("패스워드");
        }
    }


    private void validateName(String name) {
        if (name == null) {
            throw new MemberFieldRequiredException("이름");
        }
    }

    private void validateEmail(String email) {
        if (email == null) {
            throw new MemberFieldRequiredException("이메일");
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

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
