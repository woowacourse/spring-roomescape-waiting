package roomescape.reservation.dto;

import java.time.LocalDate;

import roomescape.reservation.domain.ReservationDetail;
import roomescape.theme.dto.ThemeResponse;
import roomescape.time.dto.TimeResponse;

public record ReservationDetailResponse(
        Long id,
        LocalDate date,
        TimeResponse time,
        ThemeResponse theme) {
    public static ReservationDetailResponse from(ReservationDetail detail) {
        return new ReservationDetailResponse(
                detail.getId(),
                detail.getDate(),
                TimeResponse.from(detail.getTime()),
                ThemeResponse.from(detail.getTheme()));
    }
}
