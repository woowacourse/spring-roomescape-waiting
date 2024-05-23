package roomescape.application.dto.request.reservation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Builder;
import roomescape.domain.reservationdetail.ReservationDetail;
import roomescape.domain.reservationdetail.ReservationTime;
import roomescape.domain.reservationdetail.Theme;

@Builder
public record ReservationRequest(
        @Future(message = "과거에 대한 예약은 할 수 없습니다.") @NotNull(message = "날짜에 빈값은 허용하지 않습니다.") LocalDate date,
        @Min(value = 1, message = "멤버 아이디는 1이상의 정수만 허용합니다.") Long memberId,
        @Min(value = 1, message = "타임 아이디는 1이상의 정수만 허용합니다.") Long timeId,
        @Min(value = 1, message = "테마 아이디는 1이상의 정수만 허용합니다.") Long themeId
) {
    public ReservationDetail toReservationDetail(ReservationTime time, Theme theme) {
        return new ReservationDetail(date, time, theme);
    }
}
