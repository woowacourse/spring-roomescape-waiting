package roomescape.dto.business;

import java.time.LocalDate;
import roomescape.domain.Waiting;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.request.ReservationCreationRequest;

public record ReservationCreationContent(
        Long themeId,
        LocalDate date,
        Long timeId
) {

    public ReservationCreationContent(AdminReservationRequest request) {
        this(request.themeId(), request.date(), request.timeId());
    }

    public ReservationCreationContent(ReservationCreationRequest request) {
        this(request.themeId(), request.date(), request.timeId());
    }

    public ReservationCreationContent(Waiting waiting) {
        this(waiting.getTheme().getId(), waiting.getDate(), waiting.getTime().getId());
    }
}


