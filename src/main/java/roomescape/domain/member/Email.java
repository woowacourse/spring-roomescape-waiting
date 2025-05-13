package roomescape.domain.member;

import java.util.regex.Pattern;
import roomescape.domain.BusinessRuleViolationException;

public record Email(
        String email
) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public Email {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessRuleViolationException("이메일 형식이 아닙니다.");
        }
    }
}
