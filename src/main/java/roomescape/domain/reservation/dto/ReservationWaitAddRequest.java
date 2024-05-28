package roomescape.domain.reservation.dto;

import roomescape.domain.member.domain.Member;
import roomescape.domain.reservation.domain.Reservation;
import roomescape.domain.reservation.domain.ReservationStatus;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.time.domain.ReservationTime;

import java.time.LocalDate;

public record ReservationWaitAddRequest(LocalDate date, Long timeId, Long themeId, Long memberId) {

    public ReservationWaitAddRequest {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(date + ": 예약 날짜는 현재 보다 이전일 수 없습니다");
        }
    }

    public Reservation toEntity(ReservationTime reservationTime, Theme theme, Member member) {
        return new Reservation(null, date, ReservationStatus.RESERVATION_WAIT, reservationTime, theme, member);
    }
}