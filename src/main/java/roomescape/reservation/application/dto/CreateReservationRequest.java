package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public record CreateReservationRequest(Member member, Theme theme, LocalDate date, ReservationTime time) {
}
