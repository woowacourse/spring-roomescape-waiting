package roomescape.reservation.presentation.dto;

import java.util.List;
import roomescape.reservation.application.dto.ReservationApplicationPageResult;

public record ReservationApplicationPageResponse(
        List<ReservationApplicationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static ReservationApplicationPageResponse from(ReservationApplicationPageResult result) {
        return new ReservationApplicationPageResponse(
                result.content().stream()
                        .map(ReservationApplicationResponse::from)
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}
