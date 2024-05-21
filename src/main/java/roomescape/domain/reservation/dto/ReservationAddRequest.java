package roomescape.domain.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.member.domain.Member;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.domain.Theme;

public record ReservationAddRequest(LocalDate date, Long timeId, Long themeId, Long memberId) {

    public ReservationAddRequest addMemberId(Long memberId) {
        return new ReservationAddRequest(date, timeId, themeId, memberId);
    }

    public Reservation toEntity(ReservationTime reservationTime, Theme theme, Member member,
                                ReservationStatus reservationStatus, LocalDateTime timestamp) {
        return new Reservation(null, date, reservationTime, theme, member, reservationStatus, timestamp);
    }
}
