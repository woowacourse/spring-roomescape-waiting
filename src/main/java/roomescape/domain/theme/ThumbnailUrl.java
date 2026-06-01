package roomescape.domain.theme;

import roomescape.common.exception.BadRequestException;

import java.util.Objects;
import java.util.regex.Pattern;

public class ThumbnailUrl {
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://.+");
    private final String value;

    public ThumbnailUrl(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        Objects.requireNonNull(value);
        if (!URL_PATTERN.matcher(value).matches()) {
            throw new BadRequestException("유효하지 않은 이미지 주소입니다. URL은 https로 시작해야 합니다.");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThumbnailUrl that = (ThumbnailUrl) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
