package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import roomescape.exception.ErrorType;
import roomescape.exception.InvalidClientFieldWithValueException;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;
import roomescape.exception.clienterror.InvalidIdException;

@Entity
public class Member {
    //TODO: 실제 삽입하는 데이터와 불일치 해결 (패스워드)
    private static final String ADMIN_ROLE = "ADMIN";
    private static Pattern EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$");

    @Id
    private Long id;
    private String email;
    private String name;
    private String role;

    public Member() {
    }

    public Member(final Long id, final String email, final String name, String role) {
        validId("id", id);
        validEmail(email);
        validEmpty("name", name);
        validEmpty("role", role);
        this.id = id;
        this.email = email;
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

    private void validId(final String fieldName, final Long value) {
        if (value == null || value <= 0) {
            throw new InvalidIdException(fieldName, value);
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
}
