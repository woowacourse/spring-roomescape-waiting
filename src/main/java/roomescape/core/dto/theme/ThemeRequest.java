package roomescape.core.dto.theme;

import jakarta.validation.constraints.NotBlank;

public class ThemeRequest {
    @NotBlank(message = "테마 이름은 비어있을 수 없습니다.")
    private String name;

    @NotBlank(message = "테마 설명은 비어있을 수 없습니다.")
    private String description;

    @NotBlank(message = "테마 섬네일은 비어있을 수 없습니다.")
    private String thumbnail;

    public ThemeRequest() {
    }

    public ThemeRequest(final String name, final String description, final String thumbnail) {
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
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
