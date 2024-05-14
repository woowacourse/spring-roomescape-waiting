package roomescape.service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoomTheme;

public record ReservationCreateRequest(
        @NotNull
        Long memberId,
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull
        Long timeId,
        @NotNull
        Long themeId)
{
    public static ReservationCreateRequest from(ReservationCreateMemberRequest adminRequest, Long memberId) {
        return new ReservationCreateRequest(memberId, adminRequest.date(), adminRequest.timeId(), adminRequest.themeId());
    }

    public Reservation toReservation(Member member, ReservationTime reservationTime, RoomTheme roomTheme) {
        return new Reservation(member, date, reservationTime, roomTheme);
    }
}
