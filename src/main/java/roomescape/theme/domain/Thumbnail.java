package roomescape.theme.domain;

import jakarta.persistence.Column;
import java.util.Objects;
import roomescape.global.exception.theme.InvalidThumbnailException;

public class Thumbnail {

    @Column(name = "thumbnail", nullable = false)
    private String value;

    public Thumbnail(String value) {
        validate(value);
        this.value = value;
    }

    protected Thumbnail() {
    }

    private void validate(String value) {
        validateNotNull(value);
        validateNotBlank(value);
    }

    private void validateNotNull(String value) {
        if (value == null) {
            throw new InvalidThumbnailException("썸네일은 비어있을 수 없습니다");
        }
    }

    private void validateNotBlank(String value) {
        if (value.isBlank()) {
            throw new InvalidThumbnailException("썸네일은 공백문자로만 이루어질 수 없습니다");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Thumbnail thumbnail = (Thumbnail) o;
        return Objects.equals(value, thumbnail.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
