package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import roomescape.exception.ErrorType;
import roomescape.exception.InvalidClientFieldWithValueException;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

@Entity
public class Member {
    private static final String ADMIN_ROLE = "ADMIN";
    private static Pattern EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String salt;
    private String name;
    private String role;

    public Member() {
    }

    public Member(final Long id, final String email, final String password, final String salt, final String name,
                  String role) {
        validEmail(email);
        validEmpty("name", name);
        validEmpty("role", role);
        this.id = id;
        this.email = email;
        this.password = password;
        this.salt = salt;
        this.name = name;
        this.role = role;
    }

    public Member(final String email, final Password password, final String name, final String role) {
        this(null, email, password.getHashValue(), password.getSalt(), name, role);
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
        return ADMIN_ROLE.equals(role);
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
        return new Password(this.password, this.salt);
    }
}
