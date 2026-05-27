package roomescape.domain.theme.mapper;

import org.springframework.stereotype.Component;
import roomescape.domain.theme.dto.command.ThemeCreateCommand;
import roomescape.domain.theme.dto.request.ThemeCreateRequestDto;
import roomescape.domain.theme.dto.response.ReservationThemeResponseDto;
import roomescape.domain.theme.dto.response.ThemeResponseDto;
import roomescape.domain.theme.entity.Theme;

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
            theme.getImageUrl(), theme.getDeletedAt() != null);
    }
}
