package roomescape.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String email;
    private String password;
    private String name;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    public Member(final String email, final String password, final String name, final MemberRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public boolean matchesPassword(final String password) {
        return Objects.equals(this.password, password);
    }
}
