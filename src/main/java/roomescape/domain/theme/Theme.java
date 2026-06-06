package roomescape.domain.theme;

import java.util.Objects;

public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;

    private Theme(final Long id, final String name, final String description, final String thumbnailUrl) {
        validateName(name);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    private void validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 테마 이름은 비어있을 수 없습니다.");
        }
    }

    public static Theme createNew(final String name, final String description, final String thumbnailUrl) {
        return new Theme(null, name, description, thumbnailUrl);
    }

    public static Theme of(final Long id, final String name, final String description, final String thumbnailUrl) {
        validateId(id);
        return new Theme(id, name, description, thumbnailUrl);
    }

    public Theme withId(final Long id) {
        validateId(id);
        return new Theme(id, name, description, thumbnailUrl);
    }

    private static void validateId(final Long id){
        if (id == null) {
            throw new IllegalArgumentException("Id는 비어있을 수 없습니다.");
        }
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Theme)) {
            return false;
        }
        Theme t = (Theme) o;
        return Objects.equals(id, t.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
