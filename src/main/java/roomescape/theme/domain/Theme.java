package roomescape.theme.domain;

import roomescape.theme.controller.exception.ThemeDescriptionNullException;
import roomescape.theme.controller.exception.ThemeNameNullException;
import roomescape.theme.controller.exception.ThemeThumbnailNullException;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    public Theme(Long id, String name, String description, String thumbnail) {
        validateName(name);
        validateDescription(description);
        validateThumbnail(thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ThemeNameNullException("[ERROR] 테마의 이름은 공백이 될 수 없습니다.");
        }
    }

    private void validateThumbnail(String thumbnail) {
        if (thumbnail == null || thumbnail.isBlank()) {
            throw new ThemeThumbnailNullException("[ERROR] 테마의 썸네일은 공백이 될 수 없습니다.");
        }
    }

    private void validateDescription (String description) {
        if (description == null || description.isBlank()) {
            throw new ThemeDescriptionNullException("[ERROR] 테마의 설명은 공백이 될 수 없습니다.");
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

    public String getThumbnail() {
        return thumbnail;
    }
}
