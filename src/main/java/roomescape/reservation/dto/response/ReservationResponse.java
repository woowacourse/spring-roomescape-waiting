package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.dto.ThemeResponse;

import java.time.LocalDate;

public record ReservationResponse(Long id, LocalDate date,
                                  @JsonProperty("member") MemberResponse member,
                                  @JsonProperty("time") ReservationTimeResponse time,
                                  @JsonProperty("theme") ThemeResponse theme) {

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                MemberResponse.fromEntity(reservation.getMember()),
                ReservationTimeResponse.from(reservation.getReservationTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
