package roomescape.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "member")
    private List<Reservation> reservations;

    public Member() {
    }

    @Builder
    public Member(Long id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public boolean isAdmin() {
        return role.isAdmin();
    }
}
