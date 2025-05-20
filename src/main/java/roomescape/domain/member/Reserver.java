package roomescape.domain.member;


import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Reserver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String name;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    public Reserver() {

    }

    public Reserver(Long id, String username, String password, String name, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
    }

    public static Reserver of(long id, Reserver reserver) {
        return new Reserver(id, reserver.username, reserver.password, reserver.name, reserver.role);
    }

    public boolean isSameUsername(String username) {
        return this.username.equals(username);
    }

    public String getUsername() {
        return username;
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reserver reserver = (Reserver) o;
        if (id == null && reserver.id == null) {
            return false;
        }
        return Objects.equals(id, reserver.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
