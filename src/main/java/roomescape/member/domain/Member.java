package roomescape.member.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Enumerated(value = EnumType.STRING)
    private Role role = Role.USER;
    private String name;
    private String email;
    private String password;

    public Member() {
    }

    public Member(String name, String email, String password) {
        this(null, Role.USER, name, email, password);
    }

    public Member(Long id, Role role, String name, String email, String password) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.email = email;
        this.password = password;
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

    public boolean sameMemberId(Long memberId) {
        return this.id.equals(memberId);
    }
}
