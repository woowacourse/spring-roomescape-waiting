package roomescape.member;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private final Long id;

    private String email;
    private String password;
    private String name;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    public Member() {
        id = null;
    }

    public Member(final String email, final String password, final String name, final MemberRole role) {
        this(null, email, password, name, role);
    }

    public boolean matchesPassword(final String password) {
        return Objects.equals(this.password, password);
    }
}
