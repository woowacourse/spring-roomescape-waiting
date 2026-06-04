package roomescape.dto.response;

import roomescape.domain.ReservationWaiting;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        LocalDate reservationDate,
        CreateReservationTimeResponse time,
        ThemeResponse theme,
        int order
) {
    public static ReservationWaitingResponse from(ReservationWaiting reservationWaiting, int order) {
        return new ReservationWaitingResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getCreatedAt(),
                reservationWaiting.getReservationDate(),
                new CreateReservationTimeResponse(
                        reservationWaiting.getTime().getId(),
                        reservationWaiting.getTime().getStartAt()
                ),
                new ThemeResponse(
                        reservationWaiting.getTheme().getId(),
                        reservationWaiting.getTheme().getName(),
                        reservationWaiting.getTheme().getDescription(),
                        reservationWaiting.getTheme().getThumbnail()
                ),
                order
        );
    }
}
