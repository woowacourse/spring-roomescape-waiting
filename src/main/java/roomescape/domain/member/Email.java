package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
public class Email {

    private static final int MAX_LENGTH = 255;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

    @Column(nullable = false, unique = true)
    private String email;

    protected Email() {
    }

    protected Email(String email) {
        validateBlank(email);
        validateLength(email);
        validatePattern(email);
        this.email = email;
    }

    private void validateBlank(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수 값입니다.");
        }
    }

    private void validateLength(String email) {
        if (MAX_LENGTH < email.length()) {
            throw new IllegalArgumentException(String.format("이메일은 %d자를 넘을 수 없습니다.", MAX_LENGTH));
        }
    }

    private void validatePattern(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
    }

    protected String getValue() {
        return email;
    }
}
