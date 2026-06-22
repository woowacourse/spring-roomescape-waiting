package roomescape.domain.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.regex.Pattern;

import static roomescape.domain.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.DomainPreconditions.require;
import static roomescape.domain.DomainPreconditions.requireNonBlank;

@Embeddable
public class ThumbnailUrl {
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://.+");

    @Column(name = "thumbnail_url", nullable = false)
    private String value;

    protected ThumbnailUrl() {
    }

    public ThumbnailUrl(String value) {
        requireNonBlank(value, INVALID_INPUT, "이미지 주소는 비어있을 수 없습니다.");
        require(isValidPattern(value), INVALID_INPUT, "유효하지 않은 이미지 주소입니다. URL은 https로 시작해야 합니다.");
        this.value = value;
    }

    private boolean isValidPattern(String value) {
        return URL_PATTERN.matcher(value).matches();
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
