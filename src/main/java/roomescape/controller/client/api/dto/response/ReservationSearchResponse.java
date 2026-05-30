package roomescape.controller.client.api.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.service.result.ReservationSearchResult;

public record ReservationSearchResponse(long id, String name, LocalDate date, LocalTime startAt, String theme,
                                        String status, Integer waitingRank) {

    public static ReservationSearchResponse from(ReservationSearchResult result) {
        return new ReservationSearchResponse(
                result.id(),
                result.name(),
                result.date(),
                result.startAt(),
                result.themeName(),
                result.status(),
                result.waitingRank()
        );
    }
}
