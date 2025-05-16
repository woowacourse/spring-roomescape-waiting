package roomescape.dto.business;

import roomescape.dto.request.ThemeCreationRequest;

public record ThemeCreationContent(String name, String description, String thumbnail) {

    public ThemeCreationContent(ThemeCreationRequest request) {
        this(request.name(), request.description(), request.thumbnail());
    }
}
