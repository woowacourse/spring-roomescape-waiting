package roomescape.reservation.presentation.dto;

import java.util.List;
import roomescape.reservation.application.dto.ReservationPageResult;

public record ReservationPageResponse(
        List<ReservationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static ReservationPageResponse from(ReservationPageResult result) {
        return new ReservationPageResponse(
                result.content().stream()
                        .map(ReservationResponse::from)
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}
