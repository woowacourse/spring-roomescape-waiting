package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import roomescape.auth.dto.LoginMember;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.entity.MemberReservation;
import roomescape.reservation.domain.entity.Reservation;
import roomescape.reservation.domain.entity.ReservationStatus;

import java.time.LocalDate;

public record ReservationCreateRequest(
        @NotNull(message = "사용자는 비어있을 수 없습니다.") Long memberId,
        @NotNull(message = "예약 날짜는 비어있을 수 없습니다.") LocalDate date,
        @NotNull(message = "예약 시간은 비어있을 수 없습니다.") Long timeId,
        @NotNull(message = "테마는 비어있을 수 없습니다.") Long themeId,
        @NotNull(message = "예약 상태는 비어있을 수 없습니다.") ReservationStatus status
) {

    public ReservationCreateRequest(Long memberId, LocalDate date, Long timeId, Long themeId) {
        this(memberId, date, timeId, themeId, ReservationStatus.CONFIRMATION);
    }

    public static ReservationCreateRequest of(MemberReservationCreateRequest request, LoginMember loginMember) {
        return new ReservationCreateRequest(loginMember.id(), request.date(), request.timeId(), request.themeId(), request.status());
    }

    public MemberReservation toMemberReservation(Member member, Reservation reservation) {
        return new MemberReservation(member, reservation, status);
    }
}
