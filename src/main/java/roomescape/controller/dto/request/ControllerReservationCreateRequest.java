package roomescape.controller.dto.request;

import java.time.LocalDate;

import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
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
            throw new RoomEscapeException(DomainErrorCode.INVALID_INPUT);
        }
        if (date == null) {
            throw new RoomEscapeException(DomainErrorCode.INVALID_INPUT);
        }
        if (timeId == null) {
            throw new RoomEscapeException(DomainErrorCode.INVALID_INPUT);
        }
        if (themeId == null) {
            throw new RoomEscapeException(DomainErrorCode.INVALID_INPUT);
        }
    }
}