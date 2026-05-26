package roomescape.theme.service.dto;

import roomescape.theme.domain.Theme;

public record ThemeResponseServiceDto(Long id, String name, String description, String imageUrl) {

    public static ThemeResponseServiceDto from(Theme theme) {
        return new ThemeResponseServiceDto(theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getImageUrl());
    }
}
