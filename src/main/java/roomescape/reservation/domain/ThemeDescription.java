package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class ThemeDescription {

    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 200;

    private String description;

    public ThemeDescription(final String description) {
        if (description == null || description.isBlank() || description.length() < MIN_LENGTH || description.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("테마 소개는 최소 5글자, 최대 200글자여야합니다.");
        }
        this.description = description;
    }

    public ThemeDescription() {
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ThemeDescription that = (ThemeDescription) o;
        return description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return description.hashCode();
    }
}
