package roomescape.adapter.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.application.dto.command.ThemeCreateCommand;

public class ThemeRequest {

    @NotBlank(message = "이름은 비어 있을 수 없습니다.")
    private String name;

    @NotBlank(message = "설명은 비어 있을 수 없습니다.")
    private String description;

    @NotBlank(message = "썸네일은 비어 있을 수 없습니다.")
    private String thumbnailUrl;

    public ThemeRequest() {
    }

    public ThemeCreateCommand toCommand() {
        return new ThemeCreateCommand(name, description, thumbnailUrl);
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
