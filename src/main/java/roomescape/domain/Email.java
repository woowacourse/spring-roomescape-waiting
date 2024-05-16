package roomescape.domain;

import static roomescape.exception.ExceptionType.INVALID_EMAIL_FORMAT;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import roomescape.exception.RoomescapeException;

@Embeddable
public class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    @Column(name = "email")
    private String rawEmail;

    protected Email() {
    }

    public Email(String rawEmail) {
        validateEmail(rawEmail);
        this.rawEmail = rawEmail;
    }

    private void validateEmail(String email) {
        if (email == null) {
            throw new RoomescapeException(INVALID_EMAIL_FORMAT);
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (!matcher.matches()) {
            throw new RoomescapeException(INVALID_EMAIL_FORMAT);
        }
    }

    public String getRawEmail() {
        return rawEmail;
    }
}
