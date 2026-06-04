package roomescape.reservation.presentation.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public record CreateTimeRequest(
    @NotNull(message = "시간은 필수 사항 입니다. 시간을 선택해주세요.")
    LocalTime startAt
) {

    public ReservationTime toEntity() {
        return ReservationTime.createWithoutId(startAt);
    }
}
