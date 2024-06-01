package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.theme.dto.ThemeResponse;

import java.time.LocalDate;

public record ReservationResponse(Long id, LocalDate date,
                                  @JsonProperty("member") MemberResponse member,
                                  @JsonProperty("time") ReservationTimeResponse time,
                                  @JsonProperty("theme") ThemeResponse theme) {

    public static ReservationResponse from(final MemberReservation memberReservation) {
        ReservationDetail reservation = memberReservation.getReservationDetail();
        return new ReservationResponse(
                memberReservation.getId(),
                reservation.getDate(),
                MemberResponse.fromEntity(memberReservation.getMember()),
                ReservationTimeResponse.from(reservation.getReservationTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
