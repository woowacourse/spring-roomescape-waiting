package roomescape.controller.dto;

import roomescape.domain.PopularTheme;

public record PopularThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnail,
        Long reservationCount
) {

    public static PopularThemeResponse from(PopularTheme popularTheme) {
        return new PopularThemeResponse(
                popularTheme.getTheme().getId(),
                popularTheme.getTheme().getName(),
                popularTheme.getTheme().getDescription(),
                popularTheme.getTheme().getThumbnail(),
                popularTheme.getReservationCount()
        );
    }
}
