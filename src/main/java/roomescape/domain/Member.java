package roomescape.domain;

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
    @Embedded
    private MemberEmail email;
    @Embedded
    private MemberPassword password;
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    public Member() {
    }

    public Member(Long id, MemberName name, MemberEmail email, MemberPassword password, MemberRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(MemberName name, MemberEmail email, MemberPassword password, MemberRole role) {
        this(null, name, email, password, role);
    }

    public boolean isDifferentPassword(MemberPassword otherPassword) {
        return !password.equals(otherPassword);
    }

    public Long getId() {
        return id;
    }

    public MemberName getName() {
        return name;
    }

    public MemberEmail getEmail() {
        return email;
    }

    public MemberPassword getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }
}
