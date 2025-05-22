package roomescape.presentation.dto.response;

import roomescape.business.dto.WaitingDto;

import java.time.LocalDate;
import java.util.List;

public record WaitingResponse(
        String id,
        String name,
        String themeId,
        LocalDate date,
        String timeId
) {
    public static WaitingResponse from(final WaitingDto dto) {
        return new WaitingResponse(
                dto.reservationId().value(),
                dto.userName().value(),
                dto.themeId().value(),
                dto.date().value(),
                dto.timeId().value()
        );
    }

    public static List<WaitingResponse> from(final List<WaitingDto> dtos) {
        return dtos.stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
