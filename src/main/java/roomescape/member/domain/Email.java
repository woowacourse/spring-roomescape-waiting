package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.common.exception.MemberException;
import java.util.regex.Pattern;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @Column(name = "email")
    private String value;

    public Email(String value) {
        validate(value);
        validateFormat(value);
        this.value = value;
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new MemberException("Email cannot be null or blank");
        }
    }

    private void validateFormat(String value) {
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new MemberException("Invalid email format");
        }
    }
}
