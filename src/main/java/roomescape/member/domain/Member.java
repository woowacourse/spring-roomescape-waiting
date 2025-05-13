package roomescape.member.domain;

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
    private MemberName name;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    public Member(final Long id, final String name, final String email, final String password, final MemberRole role) {
        this.id = id;
        this.name = new MemberName(name);
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member() {
    }

    public boolean hasSameId(final long other) {
        return id == other;
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

    public MemberRole getRole() {
        return role;
    }
}
