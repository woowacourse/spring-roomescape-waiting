package roomescape.domain;

import static roomescape.domain.exception.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.exception.DomainPreconditions.requireNonBlank;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;

    public Theme(Long id, String name, String description, String thumbnailUrl) {
        this.id = id;
        this.name = requireNonBlank(name, INVALID_INPUT, "테마 이름은 비어있거나 공백일 수 없습니다.");
        this.description = requireNonBlank(description, INVALID_INPUT, "테마 설명은 비어있거나 공백일 수 없습니다.");
        this.thumbnailUrl = requireNonBlank(thumbnailUrl, INVALID_INPUT, "테마 썸네일 URL은 비어있거나 공백일 수 없습니다.");
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
