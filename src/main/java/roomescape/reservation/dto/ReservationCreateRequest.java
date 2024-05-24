package roomescape.reservation.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationBuilder;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

public record ReservationCreateRequest(@NotNull(message = "예약자 정보가 없습니다.")
                                       Long memberId,
                                       @NotNull(message = "테마 정보가 입력되지 않았습니다.")
                                       Long themeId,
                                       @NotNull(message = "시간 정보가 입력되지 않았습니다.")
                                       Long timeId,
                                       @NotNull(message = "날짜가 선택되지 않습니다.")
                                       LocalDate date
) {
    public Reservation toReservation(Member member, ReservationDetail detail) {
        return new ReservationBuilder()
                .member(member)
                .reservationDetail(detail)
                .build();
    }

    public ReservationDetail toReservationDetail(Theme theme, Time time) {
        return new ReservationDetail(theme, time, date);
    }

    public boolean isBeforeDate(LocalDate newDate) {
        return date.isBefore(newDate);
    }
}
