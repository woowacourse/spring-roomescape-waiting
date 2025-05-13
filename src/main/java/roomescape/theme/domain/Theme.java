package roomescape.theme.domain;

import java.util.Objects;
import roomescape.common.exception.BusinessException;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    private Theme(final Long id, final String name, final String description, final String thumbnail) {
        validateIsNonNull(name);
        validateIsNonNull(description);
        validateIsNonNull(thumbnail);

        validateIsEmpty(name);
        validateIsEmpty(description);
        validateIsEmpty(thumbnail);

        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validateIsNonNull(final Object object) {
        if (object == null) {
            throw new BusinessException("테마 정보는 null 일 수 없습니다.");
        }
    }

    private void validateIsEmpty(final String something) {
        if (something.isEmpty()) {
            throw new BusinessException("테마 정보는 비어있을 수 없습니다.");
        }
    }

    public static Theme createWithoutId(final String name, final String description, final String thumbnail) {
        return new Theme(null, name, description, thumbnail);
    }

    public static Theme createWithId(final Long id, final String name, final String description, final String thumbnail) {
        validateIdIsNonNull(id);
        return new Theme(id, name, description, thumbnail);
    }

    private static void validateIdIsNonNull(final Long id) {
        if (id == null) {
            throw new BusinessException("테마 id는 null 일 수 없습니다.");
        }
    }

    public Theme assignId(final Long id) {
        return createWithId(id, name, description, thumbnail);
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

    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof Theme that)) {
            return false;
        }

        if (getId() == null && that.getId() == null) {
            return false;
        }
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
