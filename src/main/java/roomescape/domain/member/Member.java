package roomescape.domain.member;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

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
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public Member(String email, String password, String name, String role) {
        this(
                email,
                new MemberPassword(password),
                name,
                Role.getRole(role)
        );
    }

    public Member(String email, String password, String name) {
        this(
                email,
                new MemberPassword(password),
                name,
                Role.MEMBER
        );
    }

    protected Member() {
    }

    public boolean isMismatchedPassword(MemberPassword other) {
        return this.password.isMismatchedPassword(other);
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
}
