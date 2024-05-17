package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.exception.MemberExceptionCode;

@Embeddable
public class Password {

    private static final Pattern PASSWORD_FORM = Pattern.compile("^(?=.*\\d)(?=.*[a-z])[a-z0-9]*$");

    @Column(nullable = false)
    private String password;

    protected Password() {
    }

    private Password(String password) {
        this.password = password;
    }

    public static Password from(String password) {
        validation(password);
        return new Password(password);
    }

    public static Password from() {
        return new Password(null);
    }

    public String getPassword() {
        return password;
    }

    private static void validation(String password) {
        if (!PASSWORD_FORM.matcher(password).matches()) {
            throw new RoomEscapeException(MemberExceptionCode.ILLEGAL_PASSWORD_FORM_EXCEPTION);
        }
    }

    @Override
    public String toString() {
        return "Password{" +
                "password='" + password + '\'' +
                '}';
    }
}
