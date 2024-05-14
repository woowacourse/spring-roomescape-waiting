package roomescape.domain.reservation;

import java.util.Objects;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final Thumbnail thumbnail;

    public Theme(final Long id, final String name, final String description, final Thumbnail thumbnail) {
        validate(name, description);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public static Theme of(final Long id, final String name, final String description, final String thumbnail) {
        return new Theme(id, name, description, new Thumbnail(thumbnail));
    }

    private void validate(final String name, final String description) {
        validateNull(name, description);
    }

    private void validateNull(final String name, final String description) {
        if (name.isBlank() || description.isBlank()) {
            throw new IllegalArgumentException("이름과 설명은 공백일 수 없습니다");
        }
    }

    public boolean isEqualId(final long id){
        return this.id.equals(id);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailAsString() {
        return thumbnail.asString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Theme theme = (Theme) o;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
