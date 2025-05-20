package roomescape.reservationTime.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.common.exception.InvalidTimeException;
import roomescape.common.exception.message.RequestExceptionMessage;

public record ReservationTimeRequest(@JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public ReservationTimeRequest {
        if (startAt == null) {
            throw new InvalidTimeException(RequestExceptionMessage.INVALID_TIME.getMessage());
        }
    }
}
