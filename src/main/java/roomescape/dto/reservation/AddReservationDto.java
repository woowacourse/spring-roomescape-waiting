package roomescape.dto.reservation;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

public record AddReservationDto(@NotBlank(message = "이름이 비어있을 수 없습니다.") String name,
                                @FutureOrPresent(message = "날짜는 현재보다 미래여야합니다.") LocalDate date,
                                Long timeId,
                                Long themeId) {

    public Reservation toReservation(ReservationTime reservationTime, Theme theme) {
        return new Reservation(null, name, date, reservationTime, theme);
    }
}
