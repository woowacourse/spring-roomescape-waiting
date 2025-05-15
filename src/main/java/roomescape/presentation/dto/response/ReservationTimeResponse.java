package roomescape.presentation.dto.response;

import roomescape.business.dto.ReservationTimeDto;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

public record ReservationTimeResponse(
        String id,
        LocalTime startAt
) {
    public static ReservationTimeResponse from(ReservationTimeDto dto) {
        return new ReservationTimeResponse(
                dto.id().value(),
                dto.startTime().value()
        );
    }

    public static List<ReservationTimeResponse> from(List<ReservationTimeDto> dtos) {
        return dtos.stream()
                .map(ReservationTimeResponse::from)
                .sorted(Comparator.comparing(ReservationTimeResponse::startAt))
                .toList();
    }
}
