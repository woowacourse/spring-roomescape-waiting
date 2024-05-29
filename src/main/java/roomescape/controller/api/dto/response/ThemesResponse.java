package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.ThemeOutput;

import java.util.List;

public record ThemesResponse(List<ThemeResponse> data) {
    public static ThemesResponse from(final List<ThemeOutput> outputs) {
        return new ThemesResponse(
                outputs.stream()
                        .map(ThemeResponse::from)
                        .toList()
        );
    }
}
