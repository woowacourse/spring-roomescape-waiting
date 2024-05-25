package roomescape.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.exception.RoomescapeException;

import static roomescape.exception.ExceptionType.EMPTY_NAME;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    private Email email;

    @Embedded
    private Password encryptedPassword;

    @Enumerated(EnumType.STRING)
    private Role role;

    protected Member() {
    }

    public Member(String name, String email, String encryptedPassword) {
        this(null, name, email, encryptedPassword, Role.MEMBER);
    }

    public Member(Long id, String name, String email, String encryptedPassword) {
        this(id, name, email, encryptedPassword, Role.MEMBER);
    }

    public Member(Long id, String name, String email, String encryptedPassword, Role role) {
        validateName(name);
        this.id = id;
        this.name = name;
        this.email = new Email(email);
        this.encryptedPassword = new Password(encryptedPassword);
        this.role = role;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(EMPTY_NAME);
        }
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email.getRawEmail();
    }

    public String getEncryptedPassword() {
        return encryptedPassword.getEncryptedPassword();
    }

    public Role getRole() {
        return role;
    }
}
