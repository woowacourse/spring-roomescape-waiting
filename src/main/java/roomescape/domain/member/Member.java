package roomescape.domain.member;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private MemberName name;

    @Embedded
    private MemberEmail email;

    @Embedded
    private MemberEncodedPassword password;

    private MemberRole role;

    protected Member() {
    }

    public Member(
            final Long id,
            final MemberName name,
            final MemberEmail email,
            final MemberEncodedPassword password,
            final MemberRole role
    ) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email);
        this.password = Objects.requireNonNull(password);
        this.role = Objects.requireNonNull(role);
    }

    public boolean isMatchPassword(final MemberPassword rawPassword, final PasswordEncoder encoder) {
        return password.isMatched(rawPassword, encoder);
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

    public MemberEncodedPassword getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }
}
