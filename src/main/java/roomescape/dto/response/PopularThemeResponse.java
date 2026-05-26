package roomescape.dto.response;

import java.util.List;
import roomescape.dto.PopularThemeResult;

public record PopularThemeResponse(
        Long id,
        String name,
        String description,
        String imgUrl,
        Long rank,
        Long reservationCount
) {
    public static PopularThemeResponse from(PopularThemeResult popularThemeResult) {
        return new PopularThemeResponse(
                popularThemeResult.id(),
                popularThemeResult.name(),
                popularThemeResult.description(),
                popularThemeResult.imgUrl(),
                popularThemeResult.rank(),
                popularThemeResult.reservationCount()
        );
    }

    public static List<PopularThemeResponse> fromAll(List<PopularThemeResult> popularThemeResults) {
        return popularThemeResults.stream()
                .map(PopularThemeResponse::from)
                .toList();
    }
}
