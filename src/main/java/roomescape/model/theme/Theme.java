package roomescape.model.theme;

import roomescape.service.dto.ThemeDto;

import java.util.Objects;

public class Theme {

    private long id;
    private Name name;
    private Description description;
    private Thumbnail thumbnail;

    public Theme(long id, String name, String description, String thumbnail) {
        validateRange(id);
        this.id = id;
        this.name = new Name(name);
        this.description = new Description(description);
        this.thumbnail = new Thumbnail(thumbnail);
    }

    private Theme(String name, String description, String thumbnail) {
        this.id = 0;
        this.name = new Name(name);
        this.description = new Description(description);
        this.thumbnail = new Thumbnail(thumbnail);
    }

    private Theme() {
    }

    public static Theme from(ThemeDto themeDto) {
        return new Theme(themeDto.getName(), themeDto.getDescription(), themeDto.getThumbnail());
    }

    private void validateRange(long id) {
        if (id <= 0) {
            throw new IllegalStateException("id는 0 이하일 수 없습니다.");
        }
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name.getValue();
    }

    public String getDescription() {
        return description.getValue();
    }

    public String getThumbnail() {
        return thumbnail.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Theme theme = (Theme) o;
        return id == theme.id
                && Objects.equals(name.getValue(), theme.name.getValue())
                && Objects.equals(description.getValue(), theme.description.getValue())
                && Objects.equals(thumbnail.getValue(), theme.thumbnail.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, thumbnail);
    }

    @Override
    public String toString() {
        return "Theme{" +
                "themeId=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                '}';
    }
}
