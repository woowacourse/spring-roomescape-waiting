package roomescape.reservation.application.dto;

import java.util.List;

public record ReservationPageResult(
        List<ReservationResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static ReservationPageResult of(
            List<ReservationResult> content,
            int page,
            int size,
            long totalElements
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new ReservationPageResult(
                content,
                page,
                size,
                totalElements,
                totalPages
        );
    }
}
