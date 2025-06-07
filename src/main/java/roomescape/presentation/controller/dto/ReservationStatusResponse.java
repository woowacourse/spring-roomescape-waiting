package roomescape.presentation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import roomescape.application.dto.ReservationStatusServiceResponse;

public record ReservationStatusResponse(
        long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    public static ReservationStatusResponse from(ReservationStatusServiceResponse reservationStatus) {
        return new ReservationStatusResponse(
                reservationStatus.reservationId(),
                reservationStatus.theme(),
                reservationStatus.date(),
                reservationStatus.time(),
                reservationStatus.status()
        );
    }

    public static List<ReservationStatusResponse> sortedByDateTime(List<ReservationStatusServiceResponse> responses) {
        return responses.stream()
                .sorted(Comparator.comparing(ReservationStatusServiceResponse::date)
                        .thenComparing(ReservationStatusServiceResponse::time)
                ).map(ReservationStatusResponse::from)
                .toList();
    }
}
