package roomescape.domain;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;


public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailImageUrl;

    public Theme(Long id, String name, String description, String thumbnailImageUrl) {
        validate(name, description, thumbnailImageUrl);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    private void validate(String name, String description, String thumbnailImageUrl) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "테마 이름은 비어있을 수 없습니다.");
        }
        if (description == null || description.isBlank()) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "테마 설명은 비어있을 수 없습니다.");
        }
        if (thumbnailImageUrl == null || thumbnailImageUrl.isBlank()) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "테마 썸네일 이미지 URL은 비어있을 수 없습니다.");
        }
    }

    public Theme withId(Long id) {
        return new Theme(id, name, description, thumbnailImageUrl);
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

    public String getThumbnailImageUrl() {
        return thumbnailImageUrl;
    }
}
