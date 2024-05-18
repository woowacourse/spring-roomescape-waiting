package roomescape.member.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    private MemberRole role;

    public Member() {
    }

    public Member(String name, String email, String password) {
        this(null, name, email, password, "USER");
    }

    public Member(Long id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = MemberRole.valueOf(role);
    }

    public Member(Long id, String name, String email, String password) {
        this(id, name, email, password, "USER");
    }

    public boolean isAdmin() {
        return role.isAdmin();
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Member member)) {
            return false;
        }
        return Objects.equals(id, member.id) && Objects.equals(email, member.email) && Objects.equals(password,
                member.password)
                && Objects.equals(name, member.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, password, name);
    }

    @Override
    public String toString() {
        return "Member{" + "email='" + email + '\'' + ", id=" + id + ", name='" + name + '\'' + '}';
    }

}
