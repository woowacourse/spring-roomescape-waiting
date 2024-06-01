package roomescape.member.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import roomescape.reservation.domain.Reservation;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Enumerated(value = EnumType.STRING)
    private Role role = Role.MEMBER;
    private String name;
    private String email;
    private String password;
    @OneToMany(mappedBy = "member")
    private List<Reservation> reservations;

    protected Member() {
    }

    public Member(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Member(Long id, Role role, String name, String email, String password) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public boolean isSameId(long id) {
        return this.id == id;
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

    public Role getRole() {
        return role;
    }
}
