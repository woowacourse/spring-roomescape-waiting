package roomescape.member;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.reservation.Reservation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@AllArgsConstructor
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

    @OneToMany(mappedBy = "member")
    private final Set<Reservation> reservations;

    public Member() {
        reservations = new HashSet<>();
    }

    public Member(final Long id, final String email, final String password, final String name, final MemberRole role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.reservations = new HashSet<>();
    }

    public Member(final String email, final String password, final String name, final MemberRole role) {
        this(null, email, password, name, role, new HashSet<>());
    }

    public boolean matchesPassword(final String password) {
        return Objects.equals(this.password, password);
    }
}
