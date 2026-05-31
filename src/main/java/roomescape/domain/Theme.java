package roomescape.domain;

import roomescape.domain.exception.InvalidInputException;

public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;

    public Theme(Long id, String name, String description, String thumbnailUrl) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("테마 이름은 비어있을 수 없습니다.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidInputException("테마 설명은 비어있을 수 없습니다.");
        }
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new InvalidInputException("테마 썸네일 URL은 비어있을 수 없습니다.");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
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
