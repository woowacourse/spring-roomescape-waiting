package roomescape.controller.dto.request;

import java.time.LocalDate;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.service.dto.request.ServiceReservationCreateRequest;

public record ControllerReservationCreateRequest(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {

    public ControllerReservationCreateRequest {
        validate(name, date, timeId, themeId);
    }

    public ServiceReservationCreateRequest toServiceReservationRequest() {
        return new ServiceReservationCreateRequest(name, date, timeId, themeId);
    }

    private void validate(String name, LocalDate date, Long timeId, Long themeId) {
        if (name == null || name.isBlank()) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_NAME_NULL);
        }
        if (date == null) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_DATE_NULL);
        }
        if (timeId == null) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_TIME_NULL);
        }
        if (themeId == null) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_THEME_NULL);
        }
    }
}
