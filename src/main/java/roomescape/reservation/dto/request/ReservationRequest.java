package roomescape.reservation.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;

public record ReservationRequest(
        @NotNull(message = "예약 날짜는 null일 수 없습니다.")
        @FutureOrPresent(message = "지난 날짜에 대한 예약(Reservation) 등록 요청입니다.")
        LocalDate date,
        @NotNull(message = "예약(Reservation) 요청의 timeId는 null 일 수 없습니다.")
        Long timeId,
        @NotNull(message = "예약(Reservation) 요청의 themeId는 null 일 수 없습니다.")
        Long themeId
) {

    public ReservationDetail toEntity(final ReservationTime reservationTime, final Theme theme) {
        return new ReservationDetail(this.date, reservationTime, theme);
    }
}
