package roomescape.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;

public record ReservationResponse(long id,
                                  LocalDate date,
                                  String themeName,
                                  String themeDescription,
                                  String themeThumbnailUrl,
                                  LocalTime time) {
    public static ReservationResponse from(Reservation reservation, Theme theme) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                reservation.getTime().getStartAt()
        );
    }
}
