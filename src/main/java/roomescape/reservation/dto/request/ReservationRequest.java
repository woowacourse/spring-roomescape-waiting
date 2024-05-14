package roomescape.reservation.dto.request;

import io.micrometer.common.util.StringUtils;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;

public record ReservationRequest(
        LocalDate date,

        Long timeId,

        Long themeId
) {

    public ReservationRequest {
        if (StringUtils.isBlank(date.toString()) || StringUtils.isBlank(timeId.toString()) ||
                StringUtils.isBlank(themeId.toString())) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("공백 또는 null이 포함된 예약(Reservation) 등록 요청입니다. [values: %s]", this));
        }
    }

    public Reservation toEntity(final ReservationTime reservationTime, final Theme theme, final Member member) {
        return new Reservation(this.date, reservationTime, theme, member);
    }
}
