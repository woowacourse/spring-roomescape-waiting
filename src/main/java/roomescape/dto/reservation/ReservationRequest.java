package roomescape.dto.reservation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationName;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public record ReservationRequest(
        @NotNull(message = "[ERROR] 날짜가 없습니다.") LocalDate date,
        @NotNull(message = "[ERROR] 예약시간 id가 없습니다.")
        @PositiveOrZero(message = "[ERROR] id는 양수여야 합니다.") Long timeId,
        @NotNull(message = "[ERROR] 테마 id가 없습니다.")
        @PositiveOrZero(message = "[ERROR] id는 양수여야 합니다.") Long themeId,
        @NotNull
        @PositiveOrZero(message = "[ERROR] id는 양수여야 합니다.") Long memberId
) {

    public Reservation createWithoutId(ReservationTime reservationTime, Theme theme, ReservationName name) {
        return new Reservation(null, name, date, reservationTime, theme);
    }
}
