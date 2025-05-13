package roomescape.theme.domain;

public final class ThemeDescription {

    private final String description;

    public ThemeDescription(final String description) {
        if (description == null || description.isBlank() || description.length() < 5 || description.length() > 200) {
            throw new IllegalArgumentException("테마 소개는 최소 5글자, 최대 200글자여야합니다.");
        }
        this.description = description;
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
