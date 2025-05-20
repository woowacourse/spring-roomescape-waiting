package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.member.exception.InvalidMemberException;

@Entity
public class Member {

    private static final int MAX_NAME_LENGTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole memberRole;

    public Member(final String name, final String email, final String password,
                  final MemberRole memberRole) {
        validate(name);
        this.name = name;
        this.email = email;
        this.password = password;
        this.memberRole = memberRole;
    }

    protected Member() {
    }

    private void validate(final String name) {
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidMemberException("name은 10글자 이하이어야합니다.");
        }
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

    public MemberRole getMemberRole() {
        return memberRole;
    }
}
