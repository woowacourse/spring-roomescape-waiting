package roomescape.business.model.vo;

import static roomescape.exception.ErrorCode.EMAIL_FORMAT_INVALID;

import java.util.regex.Pattern;
import roomescape.exception.business.InvalidCreateArgumentException;

public record Email(
        String value
) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\p{L}0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    public Email {
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidCreateArgumentException(EMAIL_FORMAT_INVALID);
        }
    }
}
