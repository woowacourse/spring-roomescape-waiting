package roomescape.service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.controller.reservation.dto.request.ReservationCreateAdminRequest;
import roomescape.controller.reservation.dto.request.ReservationCreateMemberRequest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoomTheme;
import roomescape.domain.Status;

public record ReservationCreateRequest(
        @NotNull
        Long memberId,
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull
        Long timeId,
        @NotNull
        Long themeId,
        @NotNull
        Status status)
{
    public static ReservationCreateRequest of(ReservationCreateMemberRequest memberRequest, Long memberId, Status status) {
        return new ReservationCreateRequest(
                memberId,
                memberRequest.date(),
                memberRequest.timeId(),
                memberRequest.themeId(),
                status);
    }

    public static ReservationCreateRequest of(ReservationCreateAdminRequest adminRequest, Status status) {
        return new ReservationCreateRequest(
                adminRequest.memberId(),
                adminRequest.date(),
                adminRequest.timeId(),
                adminRequest.themeId(),
                status);
    }

    public Reservation toReservation(Member member, ReservationTime reservationTime, RoomTheme roomTheme) {
        return new Reservation(member, date, reservationTime, roomTheme, status);
    }
}
