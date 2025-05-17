package roomescape.domain.member;

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
    private String username;
    private String password;
    @Enumerated(value = EnumType.STRING)
    private Role role;
    private String name;

    protected Member() {

    }

    public Member(Long id, String username, String password, String name, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
    }

    public static Member of(long id, Member member) {
        return new Member(id, member.username, member.password, member.name, member.role);
    }

    public boolean isSameUsername(String username) {
        return this.username.equals(username);
    }

    public String getUsername() {
        return username;
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }
}
