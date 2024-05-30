package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Embedded
    private MemberName name;
    @Embedded
    private MemberEmail email;
    @Embedded
    private MemberPassword password;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role;

    public Member(Long id, String name, String email, String password, MemberRole role) {
        this.id = Objects.requireNonNull(id);
        this.name = new MemberName(Objects.requireNonNull(name));
        this.email = new MemberEmail(Objects.requireNonNull(email));
        this.password = new MemberPassword(Objects.requireNonNull(password));
        this.role = Objects.requireNonNull(role);
    }

    public Member(Long id, String name, String email) {
        this(id, new MemberName(name), new MemberEmail(email), MemberRole.USER);
    }

    private Member(Long id, MemberName name, MemberEmail email, MemberRole role) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email);
        this.role = Objects.requireNonNull(role);
    }

    protected Member() {
    }

    public boolean isAdmin() {
        return role.isAdmin();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.name();
    }

    public String getEmail() {
        return email.email();
    }

    public MemberRole getRole() {
        return role;
    }

    public String getPassword() {
        return password.password();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Member member = (Member) o;

        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Member{" +
               "id=" + id +
               ", name=" + name +
               ", email=" + email +
               ", password=" + password +
               ", role=" + role +
               '}';
    }
}
