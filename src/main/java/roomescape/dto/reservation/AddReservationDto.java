package roomescape.dto.reservation;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record AddReservationDto(@NotBlank(message = "이름이 비어있을 수 없습니다.") String name,
                                @NotNull @FutureOrPresent(message = "날짜는 현재보다 미래여야합니다.") LocalDate date,
                                Long timeId,
                                Long themeId) {

    public Reservation toReservation(ReservationTime reservationTime, Theme theme,
                                     ReservationStatus reservationStatus) {
        return new Reservation(null, name, date, reservationTime, theme, reservationStatus);
    }
}
