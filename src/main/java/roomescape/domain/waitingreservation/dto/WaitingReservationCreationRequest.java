package roomescape.domain.waitingreservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waitingreservation.WaitingReservation;

public record WaitingReservationCreationRequest(
    @NotBlank(message = "예약자명은 필수입니다")
    String name,

    @NotNull(message = "예약 날짜 선택은 필수입니다")
    Long dateId,

    @NotNull(message = "예약 시간 선택은 필수입니다")
    Long timeId,

    @NotNull(message = "테마 선택은 필수입니다")
    Long themeId
) {

    public WaitingReservation toEntity(
        ReservationDate date,
        ReservationTime time,
        Theme theme,
        LocalDateTime createdAt
    ) {
        return WaitingReservation.createWithoutId(name, date, time, theme, createdAt);
    }
}
