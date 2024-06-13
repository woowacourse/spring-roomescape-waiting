package roomescape.reservation.dto;

import roomescape.member.model.Member;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record SaveReservationRequest(@NotNull(message = "예약 날짜는 공백을 입력 할 수 없습니다.") LocalDate date,  Long memberId, Long timeId, Long themeId) {

    public SaveReservationRequest setMemberId(final Long memberId) {
        return new SaveReservationRequest(date, memberId, timeId, themeId);
    }

    public Reservation toReservation(
            final ReservationTime reservationTime,
            final Theme theme,
            final Member member
    ) {
        return Reservation.of(
                date,
                reservationTime,
                theme,
                member
        );
    }
}
