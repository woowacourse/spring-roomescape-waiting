package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import roomescape.reservation.domain.ReservationDetail;

public record ReservationDetailResponse(
        Long id,
        LocalDate date,
        LocalTime startAt,
        String themeName) {
    public static ReservationDetailResponse from(ReservationDetail detail) {
        return new ReservationDetailResponse(
                detail.getId(),
                detail.getDate(),
                detail.getTime().getStartAt(),
                detail.getTheme().getName());
    }
}
