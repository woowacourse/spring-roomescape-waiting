package roomescape.feature.theme.mapper;

import org.springframework.stereotype.Component;
import roomescape.feature.theme.domain.Theme;
import roomescape.global.domain.EntityStatus;
import roomescape.feature.theme.dto.command.ThemeCreateCommand;
import roomescape.feature.theme.dto.request.ThemeCreateRequestDto;
import roomescape.feature.theme.dto.response.ReservationThemeResponseDto;
import roomescape.feature.theme.dto.response.ThemeResponseDto;

@Component
public final class ThemeMapper {

    public ThemeCreateCommand toCreateCommand(ThemeCreateRequestDto requestDto) {
        return new ThemeCreateCommand(requestDto.name(), requestDto.description(), requestDto.imageUrl());
    }

    public ThemeResponseDto toResponseDto(Theme theme) {
        return new ThemeResponseDto(theme.getId(), theme.getName(), theme.getDescription(), theme.getImageUrl());
    }

    public ReservationThemeResponseDto toReservationResponseDto(Theme theme) {
        return new ReservationThemeResponseDto(theme.getId(), theme.getName(), theme.getDescription(),
            theme.getImageUrl(), theme.getStatus() == EntityStatus.DELETED);
    }
}
