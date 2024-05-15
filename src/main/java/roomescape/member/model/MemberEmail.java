package roomescape.member.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public class MemberEmail {

    private static final Pattern REGEX_PATTERN = Pattern.compile("^(.+)@(\\S+)$");

    @Column(length = 30, nullable = false)
    private String email;

    protected MemberEmail() {
    }

    public MemberEmail(final String email) {
        checkNullOrEmpty(email);
        validateEmailRegex(email);
        this.email = email;
    }

    private void checkNullOrEmpty(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일 값은 공백일 수 없습니다.");
        }
    }

    private void validateEmailRegex(final String email) {
        if (!REGEX_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        }
    }

    public String getValue() {
        return email;
    }
}
