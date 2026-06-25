package roomescape.domain;

import java.util.Optional;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;

    public Theme(Long id, String name, String description, String thumbnailUrl) {
        validateName(name);
        validateDescription(description);
        validateThumbnailUrl(thumbnailUrl);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static Theme transientOf(String name, String description, String thumbnailUrl) {
        return new Theme(null, name, description, thumbnailUrl);
    }

    public Theme renewal(String name, String description, String thumbnailUrl) {
        return new Theme(
                id,
                Optional.ofNullable(name).orElse(this.name),
                Optional.ofNullable(description).orElse(this.description),
                Optional.ofNullable(thumbnailUrl).orElse(this.thumbnailUrl)
        );
    }

    private void validateThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl == null) {
            throw new IllegalArgumentException("썸네일 URL은 필수입니다.");
        }
    }

    private void validateDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("테마 설명은 필수입니다.");
        }
    }

    private void validateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("테마 이름은 필수입니다.");
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
}
