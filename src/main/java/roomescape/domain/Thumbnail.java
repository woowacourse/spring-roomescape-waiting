package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Embeddable
public class Thumbnail {

    private static final Pattern THUMBNAIL_PATTERN = Pattern.compile("^(https?|ftp|file)://.+");

    @Column(name = "thumbnail")
    private String value;

    protected Thumbnail() {
    }

    public Thumbnail(String value) {
        validateValue(value);
        this.value = value;
    }

    private void validateValue(String value) {
        Matcher matcher = THUMBNAIL_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("썸네일 URL 형식이 올바르지 않습니다");
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
        Thumbnail other = (Thumbnail) o;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "Thumbnail{" +
               "value='" + value + '\'' +
               '}';
    }
}
