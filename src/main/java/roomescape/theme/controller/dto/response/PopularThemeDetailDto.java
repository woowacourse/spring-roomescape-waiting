package roomescape.theme.controller.dto.response;

import roomescape.theme.repository.projection.PopularThemeResult;

public record PopularThemeDetailDto(
        Long id,
        String name,
        String description,
        String thumbnailUrl,
        boolean isActive,
        Long amount,
        Long reservationCount
) {
    public static PopularThemeDetailDto from(PopularThemeResult result) {
        return new PopularThemeDetailDto(
                result.id(),
                result.name(),
                result.description(),
                result.thumbnailUrl(),
                result.isActive(),
                result.amount(),
                result.reservationCount()
        );
    }
}
