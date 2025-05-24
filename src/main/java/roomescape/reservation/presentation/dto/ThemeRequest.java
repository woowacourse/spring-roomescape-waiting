package roomescape.reservation.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public class ThemeRequest {

    @NotBlank(message = "이름은 공백일 수 없습니다")
    private final String name;

    @NotBlank(message = "설명은 공백일 수 없습니다")
    private final String description;

    @NotBlank(message = "썸네일은 공백일 수 없습니다")
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
