package roomescape.reservation.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationStatus;

public record ReservationDetail(Long reservationId,
                                String username,
                                LocalDate date,
                                Long themeId,
                                String themeName,
                                String themeDescription,
                                String thumbnailImgUrl,
                                Long timeId,
                                LocalTime startAt,
                                ReservationStatus status,
                                String orderId,
                                Long amount) {
}
