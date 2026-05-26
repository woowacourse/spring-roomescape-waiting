package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationDetail(Long reservationId,
                                String username,
                                LocalDate date,
                                Long themeId,
                                String themeName,
                                String themeDescription,
                                String thumbnailImgUrl,
                                Long timeId,
                                LocalTime startAt) {
}
