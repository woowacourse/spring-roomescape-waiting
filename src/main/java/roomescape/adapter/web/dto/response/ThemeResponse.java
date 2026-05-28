package roomescape.adapter.web.dto.response;

import roomescape.application.dto.result.ThemeResult;

public class ThemeResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;

    public ThemeResponse(Long id, String name, String description, String thumbnailUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }


    public static ThemeResponse from(ThemeResult result) {
        return new ThemeResponse(
                result.getId(),
                result.getName(),
                result.getDescription(),
                result.getThumbnailUrl()
        );
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
