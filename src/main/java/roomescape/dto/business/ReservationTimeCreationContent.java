package roomescape.dto.business;

import java.time.LocalTime;
import roomescape.dto.request.ReservationTimeCreationRequest;

public record ReservationTimeCreationContent(LocalTime startAt) {

    public ReservationTimeCreationContent(ReservationTimeCreationRequest request) {
        this(request.startAt());
    }
}
