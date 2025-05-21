package roomescape.presentation.dto.response;

import roomescape.business.dto.ReservationDto;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record ReservationResponse(
        String id,
        UserResponse user,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static ReservationResponse from(ReservationDto dto) {
        return new ReservationResponse(
                dto.id().value(),
                UserResponse.from(dto.user()),
                dto.date().value(),
                ReservationTimeResponse.from(dto.time()),
                ThemeResponse.from(dto.theme())
        );
    }

    public static List<ReservationResponse> from(List<ReservationDto> dtos) {
        return dtos.stream()
                .map(ReservationResponse::from)
                .sorted(Comparator.comparing(ReservationResponse::date))
                .toList();
    }
}
