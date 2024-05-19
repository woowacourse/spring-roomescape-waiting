package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public record ReservationCreateRequest(Long memberId,
                                       @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                       Long timeId,
                                       Long themeId) {
    public Reservation createReservation(Member member, ReservationTime time, Theme theme) {
        return new Reservation(member, date, time, theme);
    }
}
