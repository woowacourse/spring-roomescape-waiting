package roomescape.member.model;

import jakarta.persistence.Column;
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

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MemberRole role;

    @Embedded
    private MemberEmail email;

    @Embedded
    private MemberName name;

    @Embedded
    private MemberPassword password;

    protected Member() {
    }

    public Member(
            final MemberRole role,
            final String password,
            final String name,
            final String email
    ) {
        this(
                null,
                role,
                new MemberPassword(password),
                new MemberName(name),
                new MemberEmail(email)
        );
    }

    public Member(
            final Long id,
            final MemberRole role,
            final String password,
            final String name,
            final String email
    ) {
        this(
                id,
                role,
                new MemberPassword(password),
                new MemberName(name),
                new MemberEmail(email)
        );
    }

    private Member(
            final Long id,
            final MemberRole role,
            final MemberPassword password,
            final MemberName name,
            final MemberEmail email
    ) {
        this.id = id;
        this.role = role;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    public MemberEmail getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public MemberName getName() {
        return name;
    }

    public MemberPassword getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }
}
