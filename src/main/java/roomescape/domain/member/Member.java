package roomescape.domain.member;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
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
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    public Member(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    protected Member() {
    }

    public static Member createUser(String name, String email, String password) {
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

    public String getRole() {
        return role.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Member)) {
            return false;
        }
        Member member = (Member) o;
        return this.getId() != null && Objects.equals(this.getId(), member.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
