package roomescape.member.entity;

import jakarta.persistence.*;
import roomescape.reservation.entity.Role;

@Entity
@Table(name = "MEMBER")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PASSWORD")
    private String password;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "ROLE")
    private Role role;

    public Member() {
    }

    public Member(
            String name,
            String email,
            String password,
            Role role
    ) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static Member createUser(
            String name,
            String email,
            String password
    ) {
        return new Member(name, email, password, Role.USER);
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
