package roomescape.reservation.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.model.Reservation;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.theme.model.Theme;

public record CreateReservationRequest(
        @FutureOrPresent(message = "예약 날짜는 현재보다 과거일 수 없습니다.")
        @NotNull(message = "예약 등록 시 예약 날짜는 필수입니다.")
        LocalDate date,

        @Positive(message = "예약 시간 식별자는 양수만 가능합니다.")
        @NotNull(message = "예약 등록 시 시간은 필수입니다.")
        Long timeId,

        @Positive(message = "예약 테마 식별자는 양수만 가능합니다.")
        @NotNull(message = "예약 등록 시 테마는 필수입니다.")
        Long themeId) {

    public Reservation toReservation(final Member member, final ReservationTime reservationTime, final Theme theme) {
        return Reservation.create(
                member,
                this.date,
                reservationTime,
                theme);
    }
}
