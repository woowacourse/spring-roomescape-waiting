package roomescape.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Getter
    @Column(nullable = false)
    private String name;

    @Getter
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Getter
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public Member(final String name, final String email, final String password, final Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public boolean hasSamePassword(final String comparedPassword) {
        return this.password.equals(comparedPassword);
    }

}
