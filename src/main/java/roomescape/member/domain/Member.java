package roomescape.member.domain;

import jakarta.persistence.Column;
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

    @Embedded
    private MemberName name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Member(Long id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = new MemberName(name);
        this.email = email;
        this.password = password;
        this.role = role;
    }

    protected Member() {

    }

    public static Member createWithId(Long id, String name, String email, String password, Role role) {
        return new Member(id, name, email, password, role);
    }

    public static Member createWithoutId(String name, String email, String password, Role role) {
        return new Member(null, name, email, password, role);
    }

    public Member assignId(Long id) {
        return new Member(id, name.getName(), email, password, role);
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

    public String getRole() {
        return role.getRole();
    }

    public Long getId() {
        return id;
    }
}
