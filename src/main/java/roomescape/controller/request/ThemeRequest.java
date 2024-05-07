package roomescape.controller.request;

import jakarta.validation.constraints.NotBlank;

public class ThemeRequest {

    @NotBlank(message = "테마의 이름은 null 혹은 빈 문자열일 수 없습니다.")
    private final String name;
    @NotBlank(message = "테마의 설명은 null 혹은 빈 문자열일 수 없습니다.")
    private final String description;
    @NotBlank(message = "테마의 썸네일은 null 혹은 빈 문자열일 수 없습니다.")
    private final String thumbnail;

    public ThemeRequest(String name, String description, String thumbnail) {
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
