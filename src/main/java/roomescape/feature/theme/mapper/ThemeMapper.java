package roomescape.feature.theme.mapper;

import org.springframework.stereotype.Component;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.theme.domain.ThemeDescription;
import roomescape.feature.theme.domain.ThemeImageUrl;
import roomescape.feature.theme.domain.ThemeName;
import roomescape.global.domain.EntityStatus;
import roomescape.feature.theme.dto.command.ThemeCreateCommand;
import roomescape.feature.theme.dto.request.ThemeCreateRequestDto;
import roomescape.feature.theme.dto.response.ReservationThemeResponseDto;
import roomescape.feature.theme.dto.response.ThemeResponseDto;

@Component
public final class ThemeMapper {

    public ThemeCreateCommand toCreateCommand(ThemeCreateRequestDto requestDto) {
        return new ThemeCreateCommand(
            new ThemeName(requestDto.name()),
            new ThemeDescription(requestDto.description()),
            new ThemeImageUrl(requestDto.imageUrl())
        );
    }

    public ThemeResponseDto toResponseDto(Theme theme) {
        return new ThemeResponseDto(theme.getId(), theme.getName(), theme.getDescription(), theme.getImageUrl(),
            theme.getStatus() == EntityStatus.DELETED);
    }

    public ReservationThemeResponseDto toReservationResponseDto(Theme theme) {
        return new ReservationThemeResponseDto(theme.getId(), theme.getName(), theme.getDescription(),
            theme.getImageUrl(), theme.getStatus() == EntityStatus.DELETED);
    }
}
