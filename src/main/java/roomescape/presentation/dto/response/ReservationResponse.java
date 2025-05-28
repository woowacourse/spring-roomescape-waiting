package roomescape.presentation.dto.response;

import roomescape.business.dto.ReservationDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

public record ReservationResponse(
        String id,
        String userName,
        LocalDate date,
        LocalTime startAt,
        String themeName
) {
    public static ReservationResponse from(final ReservationDto dto) {
        return new ReservationResponse(
                dto.id().value(),
                dto.userName().value(),
                dto.date().value(),
                dto.time().value(),
                dto.themeName().value()
        );
    }

    public static List<ReservationResponse> from(final List<ReservationDto> dtos) {
        return dtos.stream()
                .map(ReservationResponse::from)
                .sorted(Comparator.comparing(ReservationResponse::date))
                .toList();
    }
}
