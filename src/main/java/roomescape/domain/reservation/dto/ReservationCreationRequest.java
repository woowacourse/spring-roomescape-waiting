package roomescape.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record ReservationCreationRequest(
    @NotBlank(message = "예약자명은 필수입니다")
    String name,

    @NotNull(message = "예약 날짜 선택은 필수입니다")
    Long dateId,

    @NotNull(message = "예약 시간 선택은 필수입니다")
    Long timeId,

    @NotNull(message = "테마 선택은 필수입니다")
    Long themeId
) {

    public Reservation toEntity(ReservationDate reservationDate, ReservationTime reservationTime, Theme theme) {
        return Reservation.createWithoutId(
            name,
            reservationDate,
            reservationTime,
            theme
        );
    }
}
