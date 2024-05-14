package roomescape.controller.api.dto.response;

import java.util.List;

import roomescape.service.dto.output.ThemeOutput;

public record ThemeResponse(long id, String name, String description, String thumbnail) {

    public static ThemeResponse toResponse(final ThemeOutput output) {
        return new ThemeResponse(output.id(), output.name(), output.description(), output.thumbnail());
    }

    public static List<ThemeResponse> toResponses(final List<ThemeOutput> outputs) {
        return outputs.stream()
                .map(ThemeResponse::toResponse)
                .toList();
    }
}
