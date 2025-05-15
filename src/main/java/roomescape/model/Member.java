package roomescape.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String password;

    @Enumerated(value = EnumType.STRING)
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
