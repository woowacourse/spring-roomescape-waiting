package roomescape.controller.dto.request;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;

public record ReservationTimeCreateRequest(
        LocalTime startAt
) {

    public ReservationTimeCreateRequest {
        validate(startAt);
    }

    public ReservationTime toEntity() {
        return new ReservationTime(startAt);
    }

    private void validate(LocalTime startAt) {
        if (startAt == null) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_TIME_NULL);
        }
    }
}
