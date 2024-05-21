package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import roomescape.auth.dto.LoginMember;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;

public record ReservationCreateRequest(
        @NotNull(message = "사용자는 비어있을 수 없습니다.") Long memberId,
        @NotNull(message = "예약 날짜는 비어있을 수 없습니다.") LocalDate date,
        @NotNull(message = "예약 시간은 비어있을 수 없습니다.") Long timeId,
        @NotNull(message = "테마는 비어있을 수 없습니다.") Long themeId
) {

    public static ReservationCreateRequest of(MemberReservationCreateRequest request, LoginMember loginMember) {
        return new ReservationCreateRequest(loginMember.id(), request.date(), request.timeId(), request.themeId());
    }

    public Reservation toReservation(Member member, ReservationTime time, Theme theme) {
        return new Reservation(member, date, time, theme);
    }
}
