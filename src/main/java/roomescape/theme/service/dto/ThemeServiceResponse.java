package roomescape.theme.service.dto;

import roomescape.theme.domain.Theme;

public record ThemeServiceResponse(Long id, String name, String description, String imageUrl) {

    public static ThemeServiceResponse from(Theme theme) {
        return new ThemeServiceResponse(theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getImageUrl());
    }
}
