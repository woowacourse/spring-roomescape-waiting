package roomescape.domain.member;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private Name name;
    @Embedded
    private Email email;
    @Embedded
    private Password password;
    @Enumerated(EnumType.STRING)
    private Role role;

    public Member(Long id, String rawName, String rawEmail, String rawPassword, Role role) {
        this.id = id;
        this.name = new Name(rawName);
        this.email = new Email(rawEmail);
        this.password = new Password(rawPassword);
        this.role = role;
    }

    protected Member() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getValue();
    }

    public String getEmail() {
        return email.getValue();
    }

    public String getPassword() {
        return password.getValue();
    }

    public String getRole() {
        return role.toString();
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
