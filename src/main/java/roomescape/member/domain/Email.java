package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Embeddable
public record Email(
        @Column(nullable = false, unique = true)
        String email
) {
    private static final Pattern emailPattern = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");

    public Email {
        validateEmail(email);
    }

    private void validateEmail(String email) {
        validateEmailIsNull(email);
        validateEmailIsInvalidType(email);
    }

    private void validateEmailIsNull(String email) {
        if (email == null) {
            throw new IllegalArgumentException("회원 생성 시 이메일 필수입니다.");
        }
    }

    private void validateEmailIsInvalidType(String email) {
        Matcher matcher = emailPattern.matcher(email);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(email + "은 이메일 형식이 아닙니다.");
        }
    }
}
