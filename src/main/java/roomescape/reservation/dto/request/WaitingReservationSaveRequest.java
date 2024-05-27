package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.Theme;

public record WaitingReservationSaveRequest(
        @NotNull Long memberId,
        @NotNull LocalDate date,
        @NotNull Long themeId,
        @NotNull Long timeId
) {

    public static WaitingReservationSaveRequest of(ReservationDetailRequest detail, Long memberId) {
        return new WaitingReservationSaveRequest(
                memberId,
                detail.date(),
                detail.themeId(),
                detail.timeId()
        );
    }

    public Reservation toWaitingReservation(Member member, Theme theme, ReservationTime reservationTime) {
        return new Reservation(member, date, theme, reservationTime, Status.WAIT);
    }
}
