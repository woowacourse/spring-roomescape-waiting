package roomescape.controller.client.api.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationSearchResponse(long id, String name, LocalDate date, LocalTime startAt, String theme,
                                        String status, Integer waitingRank, String orderId, String orderStatus,
                                        String paymentKey, Long paymentAmount, String paymentStatus) {

    public ReservationSearchResponse(long id, String name, LocalDate date, LocalTime startAt, String theme,
                                     String status, Integer waitingRank) {
        this(id, name, date, startAt, theme, status, waitingRank, null, null, null, null, null);
    }
}
