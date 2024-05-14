package roomescape.reservation.dto.request;

import io.micrometer.common.util.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;
import roomescape.reservation.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeRequest(
        @DateTimeFormat(pattern = "kk:mm")
        LocalTime startAt
) {

    public ReservationTimeRequest {
        if (StringUtils.isBlank(startAt.toString())) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("공백 또는 null이 포함된 예약 시간(ReservationTime) 등록 요청입니다. [values: %s]", this));
        }
    }

    public ReservationTime toTime() {
        return new ReservationTime(this.startAt);
    }
}
