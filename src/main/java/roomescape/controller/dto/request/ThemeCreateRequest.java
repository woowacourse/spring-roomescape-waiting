package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public class ThemeCreateRequest {
    @NotNull(message = "이름은 필수로 입력해야 합니다")
    private final String name;

    @NotNull(message = "설명은 필수로 입력해야 합니다")
    private final String description;

    @NotNull(message = "URL은 필수로 입력해야 합니다")
    private final String thumbnailUrl;

    public ThemeCreateRequest(String name, String description, String thumbnailUrl) {
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
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
