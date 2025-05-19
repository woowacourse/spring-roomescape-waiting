package roomescape.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "role")
    private String role;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    public Member(final Long id, final String name, final String role, final String email, final String password) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    public Member(final String name, final String role, final String email, final String password) {
        this(null, name, role, email, password);
    }

    public Member(final Long id) {
        this.id = id;
        this.name = null;
        this.role = null;
        this.email = null;
        this.password = null;
    }

    public Member() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
