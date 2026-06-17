package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.dto.ReservationDetail;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public record ReservationResponse(
        Long id,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ReservationTheme theme,
        ReservationTimeSlot time,
        ReservationStatus status
) {
    public static ReservationResponse from(ReservationDetail detail) {
        return new ReservationResponse(
                detail.reservationId(), detail.username(), detail.date(),
                new ReservationTheme(detail.themeId(), detail.themeName()),
                new ReservationTimeSlot(detail.timeId(), detail.startAt()),
                detail.status()
        );
    }

    public static ReservationResponse from(Reservation reservation, Theme theme, ReservationTime time) {
        return new ReservationResponse(
                reservation.getId(), reservation.getName(), reservation.getDate(),
                new ReservationTheme(theme.getId(), theme.getName()),
                new ReservationTimeSlot(time.getId(), time.getStartAt()),
                reservation.getStatus()
        );
    }

    public static ReservationResponse from(Waiting waiting, Theme theme, ReservationTime time) {
        return new ReservationResponse(
                waiting.getId(), waiting.getName(), waiting.getDate(),
                new ReservationTheme(theme.getId(), theme.getName()),
                new ReservationTimeSlot(time.getId(), time.getStartAt()),
                ReservationStatus.CONFIRMED
        );
    }

    private record ReservationTheme(Long id, String name) {
    }

    private record ReservationTimeSlot(
            Long id,
            @JsonFormat(pattern = "HH:mm") LocalTime startAt
    ) {
    }
}
