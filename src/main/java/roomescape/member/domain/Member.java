package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.auth.domain.Role;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Embedded
    @Column(nullable = false)
    private MemberName name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    protected Member() {
    }

    public static Member createMemberByUserRole(MemberName name, String email, String password) {
        return new Member(Role.USER, name, email, password);
    }

    public Member(Role role, MemberName name, String email, String password) {
        this.role = role;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Member(Long id, Role role, MemberName name, String email, String password) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public boolean sameMemberId(Long otherMemberId) {
        return id.equals(otherMemberId);
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
}
