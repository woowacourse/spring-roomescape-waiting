package roomescape.domain.waitingreservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.waitingreservation.WaitingReservation;

public record WaitingReservationWithRankResponse(
    Long id,
    String name,
    LocalDate date,
    ReservationResponse.ReservationTimePayload time,
    ReservationResponse.ThemePayload theme,
    Long rank,
    LocalDateTime createdAt
) {
    public static WaitingReservationWithRankResponse from(WaitingReservationWithRank waitingReservationWithRank) {
        WaitingReservation waitingReservation = waitingReservationWithRank.waitingReservation();
        return new WaitingReservationWithRankResponse(
            waitingReservation.getId(),
            waitingReservation.getMember().getName(),
            waitingReservation.getDate().getPlayDay(),
            ReservationResponse.ReservationTimePayload.from(waitingReservation.getTime()),
            ReservationResponse.ThemePayload.from(waitingReservation.getTheme()),
            waitingReservationWithRank.rank(),
            waitingReservation.getCreatedAt()
        );
    }
}
