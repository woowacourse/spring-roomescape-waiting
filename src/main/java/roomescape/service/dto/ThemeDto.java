package roomescape.service.dto;

import roomescape.controller.request.ThemeRequest;

public class ThemeDto {

    private final Long themeId;
    private final String name;
    private final String description;
    private final String thumbnail;

    private ThemeDto(Long themeId, String name, String description, String thumbnail) {
        this.themeId = themeId;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public ThemeDto(String name, String description, String thumbnail) {
        this.themeId = null;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public static ThemeDto from(ThemeRequest themeRequest) {
        return new ThemeDto(null, themeRequest.getName(), themeRequest.getDescription(), themeRequest.getThumbnail());
    }

    public Long getThemeId() {
        return themeId;
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
