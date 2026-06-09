package roomescape.controller.dto.request;

import java.time.LocalTime;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.service.dto.request.ServiceReservationTimeCreateRequest;

public record ControllerReservationTimeCreateRequest(
        LocalTime startAt
) {

    public ControllerReservationTimeCreateRequest {
        validate(startAt);
    }

    public ServiceReservationTimeCreateRequest toServiceReservationTimeRequest() {
        return new ServiceReservationTimeCreateRequest(startAt);
    }

    private void validate(LocalTime startAt) {
        if (startAt == null) {
            throw new RoomEscapeException(DomainErrorCode.INVALID_INPUT);
        }
    }
}
