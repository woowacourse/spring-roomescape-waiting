package roomescape.domain.reservationslot.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record ReservationSlotResponse(
    Long id,
    LocalDate date,
    ReservationTimePayload time,
    ThemePayload theme
) {

    public static ReservationSlotResponse from(ReservationSlot reservation) {
        return new ReservationSlotResponse(
            reservation.getId(),
            reservation.getDate().getDate(),
            ReservationTimePayload.from(reservation.getTime()),
            ThemePayload.from(reservation.getTheme())
        );
    }

    public record ReservationTimePayload(
        Long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
    ) {

        public static ReservationTimePayload from(ReservationTime reservationTime) {
            return new ReservationTimePayload(reservationTime.getId(), reservationTime.getStartAt());
        }
    }

    public record ThemePayload(
        Long id,
        String name,
        String content,
        String url
    ) {

        public static ThemePayload from(Theme theme) {
            return new ThemePayload(
                theme.getId(),
                theme.getName(),
                theme.getContent(),
                theme.getUrl()
            );
        }
    }
}
