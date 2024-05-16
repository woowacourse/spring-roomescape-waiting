package roomescape.domain;

import static roomescape.domain.Role.MEMBER;
import static roomescape.exception.ExceptionType.EMPTY_NAME;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import roomescape.exception.RoomescapeException;

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
        this(null, name, email, encryptedPassword, MEMBER);
    }

    public Member(Long id, String name, String email, String encryptedPassword) {
        this(id, name, email, encryptedPassword, MEMBER);
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

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Member member = (Member) o;

        return Objects.equals(id, member.id);
    }
    // TODO: 제거
    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", encryptedPassword='" + encryptedPassword + '\'' +
                ", role=" + role +
                '}';
    }
}
