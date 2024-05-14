package roomescape.member.domain;

import java.util.regex.Pattern;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.exception.MemberExceptionCode;

public class Email {

    private static final Pattern EMAIL_FORM = Pattern.compile("^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$");

    private final String email;

    private Email(String email) {
        this.email = email;
    }

    public static Email saveEmailFrom(String email) {
        return new Email(email);
    }

    public static Email emailFrom(String email) {
        validate(email);
        return new Email(email);
    }

    private static void validate(String email) {
        if (!EMAIL_FORM.matcher(email).matches()) {
            throw new RoomEscapeException(MemberExceptionCode.ILLEGAL_EMAIL_FORM_EXCEPTION);
        }
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Email{" +
                "email='" + email + '\'' +
                '}';
    }
}
