package roomescape.theme.presentation.dto;

import roomescape.theme.application.dto.PopularThemeResult;

public record PopularThemeResponse(Long id, String name, String description, String thumbnailImgUrl, int reservedCount) {

    public static PopularThemeResponse from(PopularThemeResult result) {
        return new PopularThemeResponse(result.id(), result.name(), result.description(), result.thumbnailImgUrl(), result.reservedCount());
    }
}
