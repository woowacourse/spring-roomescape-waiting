package roomescape.reservation.application.dto;

import java.util.List;

public record ReservationApplicationPageResult(
        List<ReservationApplicationResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static ReservationApplicationPageResult of(
            List<ReservationApplicationResult> content,
            int page,
            int size,
            long totalElements
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new ReservationApplicationPageResult(
                content,
                page,
                size,
                totalElements,
                totalPages
        );
    }
}
