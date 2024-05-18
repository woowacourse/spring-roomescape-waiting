package roomescape.member.domain;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

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

    public Member(Long id, String name, String email, String password) {
        this(id, name, email, password, "USER");
    }

    public Member(Long id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = MemberRole.valueOf(role);
    }

    public boolean isAdmin() {
        return role.isAdmin();
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

    public MemberRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member member)) return false;

        if(id == null || member.id == null) {
            return Objects.equals(email, member.email);
        }
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        if(id == null) return Objects.hash(email);
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Member{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}
