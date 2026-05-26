package roomescape.controller.dto.request;

import java.time.LocalDate;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.service.dto.request.ServiceReservationUpdateRequest;

public record ControllerReservationUpdateRequest(
        LocalDate date,
        Long timeId
) {

    public ControllerReservationUpdateRequest {
        validate(date, timeId);
    }

    public ServiceReservationUpdateRequest toServiceReservationRequest() {
        return new ServiceReservationUpdateRequest(date, timeId);
    }

    private void validate(LocalDate date, Long timeId) {
        if (date == null) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_DATE_NULL);
        }
        if (timeId == null) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_TIME_NULL);
        }
    }
}
