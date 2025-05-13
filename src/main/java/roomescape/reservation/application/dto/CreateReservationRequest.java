package roomescape.reservation.application.dto;

import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public record CreateReservationRequest(Member member, Theme theme, ReservationDate date, ReservationTime time) {
}
