package roomescape.member.model;

import java.util.regex.Pattern;

public record MemberEmail(String value) {

    private static final Pattern REGEX_PATTERN = Pattern.compile("^(.+)@(\\S+)$");

    public MemberEmail {
        checkNullOrEmpty(value);
        validateEmailRegex(value);
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
}
