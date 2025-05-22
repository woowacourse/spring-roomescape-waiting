package roomescape.presentation.dto.response;

import roomescape.business.dto.WaitingDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record WaitingResponse(
        String id,
        String name,
        String themeName,
        LocalDate date,
        LocalTime time
) {
    public static WaitingResponse from(final WaitingDto dto) {
        return new WaitingResponse(
                dto.reservationId().value(),
                dto.userName().value(),
                dto.themeName().value(),
                dto.date().value(),
                dto.time().value()
        );
    }

    public static List<WaitingResponse> from(final List<WaitingDto> dtos) {
        return dtos.stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
