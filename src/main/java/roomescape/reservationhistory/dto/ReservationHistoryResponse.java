package roomescape.reservationhistory.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import roomescape.reservationhistory.ReservationHistory;
import roomescape.reservationhistory.ReservationHistoryStatus;

public record ReservationHistoryResponse(
        Long id,
        Long reservationId,
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId,
        Long storeId,
        ReservationHistoryStatus status,
        Long actorId,
        LocalDateTime createdAt
) {
    public static ReservationHistoryResponse from(ReservationHistory history) {
        return new ReservationHistoryResponse(
                history.getId(),
                history.getReservationId(),
                history.getMemberId(),
                history.getDate(),
                history.getTimeId(),
                history.getThemeId(),
                history.getStoreId(),
                history.getStatus(),
                history.getActorId(),
                history.getCreatedAt()
        );
    }

    public static List<ReservationHistoryResponse> fromAll(List<ReservationHistory> histories) {
        return histories.stream()
                .map(ReservationHistoryResponse::from)
                .toList();
    }
}
