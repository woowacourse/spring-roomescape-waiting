package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import roomescape.exception.ErrorType;
import roomescape.exception.InvalidClientFieldWithValueException;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "member")
public class Member {
    private static Pattern EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Embedded
    private Password password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    protected Member() {
    }

    public Member(final String email, final Password password, final String name, final Role role) {
        this(null, email, password, name, role);
    }

    public Member(final Long id, final String email, final Password password, final String name, final Role role) {
        validEmail(email);
        validEmpty("name", name);
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    private void validEmail(String email) {
        validEmpty("email", email);
        Matcher matcher = EMAIL_REGEX.matcher(email);
        if (!matcher.matches()) {
            throw new InvalidClientFieldWithValueException(ErrorType.INVALID_DATA_TYPE, "email", email);
        }
    }

    private void validEmpty(final String fieldName, final String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new EmptyValueNotAllowedException(fieldName);
        }
    }

    public boolean isAdmin() {
        return role.isAdmin();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Password getPassword() {
        return password;
    }
}
