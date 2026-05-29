package roomescape.controller.dto.request;

import java.time.LocalTime;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
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
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_TIME_NULL);
        }
    }
}
