package roomescape.domain.reservation;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import roomescape.exception.InvalidReservationException;

@Embeddable
public class Thumbnail {
    private static final String THUMBNAIL_REGEX = "^$|(https?|ftp)://.*\\.(jpeg|jpg|png|gif|bmp)$";

    private String value;

    public Thumbnail() {
    }

    public Thumbnail(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        Pattern pattern = Pattern.compile(THUMBNAIL_REGEX);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches()) {
            throw new InvalidReservationException("올바르지 않은 썸네일 형식입니다.");
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Thumbnail that = (Thumbnail) o;
        return Objects.equals(value, that.value);
    }

    public String getValue() {
        return value;
    }
}
