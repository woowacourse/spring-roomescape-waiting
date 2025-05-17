package roomescape.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import roomescape.exception.InvalidInputException;

@Embeddable
public record Email(
    @Column(name = "email", unique = true, nullable = false, length = 50)
    String value
) {

    private static final Pattern VALID_PATTERN = Pattern.compile("\\w+@\\w+\\.\\w+");

    public Email {
        if (!VALID_PATTERN.matcher(value).matches()) {
            throw new InvalidInputException("잘못된 형식의 이메일입니다 : " + value);
        }
    }
}
