package roomescape.reservation.application.dto.request;

import java.time.LocalDate;

public class CreateWaitingServiceRequest {

    public CreateWaitingServiceRequest(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
    ) {
    }
}
