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

    public Member(Long id, String name, String email) {
        this(id, new MemberName(name), new MemberEmail(email), MemberRole.USER);
    }

    public Member(Long id, String name, String email, String role) {
        this(id, new MemberName(name), new MemberEmail(email), MemberRole.valueOf(role));
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
        return email.value();
    }

    public MemberRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Member member = (Member) object;
        return Objects.equals(id, member.id)
               && Objects.equals(name, member.name)
               && Objects.equals(email, member.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email);
    }
}
