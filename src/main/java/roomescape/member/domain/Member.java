package roomescape.member.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.member.role.Role;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Name name;
    private Email email;
    private Password password;
    @Enumerated(EnumType.STRING)
    private Role role;

    public Member() {

    }

    public Member(Long id, Name name, Email email, Password password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(Name name, Email email, Password password, Role role) {
        this(null, name, email, password, role);
    }

    public String getName() {
        return name.getName();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email.getEmail();
    }

    public String getPassword() {
        return password.getPassword();
    }

    public String getRole() {
        return role.getRole();
    }
}
