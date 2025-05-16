package roomescape.dto.request;

import java.time.LocalDate;

public record ReservationCreationRequest(
        Long themeId,
        LocalDate date,
        Long timeId
) {

    public ReservationCreationRequest(AdminReservationRequestDto request) {
        this(request.themeId(), request.date(), request.timeId());
    }
}


