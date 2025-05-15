package roomescape.presentation.dto.response;

import roomescape.business.dto.ThemeDto;

import java.util.Comparator;
import java.util.List;

public record ThemeResponse(
        String id,
        String name,
        String description,
        String thumbnail
) {
    public static ThemeResponse from(ThemeDto dto) {
        return new ThemeResponse(dto.id().value(), dto.name().value(), dto.description(), dto.thumbnail());
    }

    public static List<ThemeResponse> from(List<ThemeDto> dtos) {
        return dtos.stream()
                .map(ThemeResponse::from)
                .sorted(Comparator.comparing(ThemeResponse::name))
                .toList();
    }
}
