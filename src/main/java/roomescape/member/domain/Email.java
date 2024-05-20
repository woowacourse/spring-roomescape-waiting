package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Embeddable
public class Email {
    private static final Pattern emailPattern = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");

    @Column(nullable = false, unique = true)
    private String email;

    public Email(final String email) {
        validateEmail(email);
        this.email = email;
    }

    protected Email() {
    }

    private void validateEmail(final String email) {
        validateEmailIsNull(email);
        validateEmailIsInvalidType(email);
    }

    private void validateEmailIsNull(final String email) {
        if (email == null) {
            throw new IllegalArgumentException("회원 생성 시 이메일 필수입니다.");
        }
    }

    private void validateEmailIsInvalidType(final String email) {
        Matcher matcher = emailPattern.matcher(email);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(email + "은 이메일 형식이 아닙니다.");
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Email email1 = (Email) o;
        return Objects.equals(email, email1.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
