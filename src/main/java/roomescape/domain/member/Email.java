package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import roomescape.domain.BusinessRuleViolationException;

@Embeddable
public record Email(
        @Column(name = "email")
        String value
) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public Email {
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new BusinessRuleViolationException("이메일 형식이 아닙니다.");
        }
    }
}
