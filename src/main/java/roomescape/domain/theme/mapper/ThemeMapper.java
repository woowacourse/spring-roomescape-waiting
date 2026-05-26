package roomescape.domain.theme.mapper;

import roomescape.domain.theme.dto.response.ReservationThemeResponseDto;
import roomescape.domain.theme.dto.response.ThemeResponseDto;
import roomescape.domain.theme.entity.Theme;

public final class ThemeMapper {

    private ThemeMapper() {

    }

    public static ThemeResponseDto toResponseDto(Theme theme) {
        return new ThemeResponseDto(theme.getId(), theme.getName(), theme.getDescription(), theme.getImageUrl());
    }

    public static ReservationThemeResponseDto toReservationResponseDto(Theme theme) {
        return new ReservationThemeResponseDto(theme.getId(), theme.getName(), theme.getDescription(),
            theme.getImageUrl(), theme.getDeletedAt() != null);
    }
}
