package roomescape.controller.dto.request;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;
import roomescape.exception.custom.InvalidRequestArgumentException;

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
            throw new InvalidRequestArgumentException("예약 시간은 비어 있을 수 없습니다.");
        }
    }
}
