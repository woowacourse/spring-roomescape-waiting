package roomescape.domain;

import jakarta.persistence.Column;
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
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;

    public Member() {
    }

    public Member(long id, Member member) {
        this(id, member.getName(), member.getRole(), member.email, member.password);
    }

    public Member(Long id, String name, Role role, String email, String password) {
        this(new LoginMember(id, name, role), email, password);
    }

    public Member(LoginMember loginMember, String email, String password) {
        this.id = loginMember.getId();
        this.name = loginMember.getName();
        this.role = loginMember.getRole();
        this.email = email;
        this.password = password;
    }

    public LoginMember getLoginMember() {
        return new LoginMember(id, name, role);
    }

    public Role getRole() {
        return role;
    }

    public long getId() {
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

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
