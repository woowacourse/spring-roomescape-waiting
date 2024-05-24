package roomescape.dto.reservation;

import java.time.LocalDate;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;

public record ReservationRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        Long timeId,
        Long themeId,
        Long memberId
) {

    public ReservationRequest {
        Objects.requireNonNull(date);
        Objects.requireNonNull(timeId);
        Objects.requireNonNull(themeId);
        Objects.requireNonNull(memberId);
    }

    public static ReservationRequest from(UserReservationRequest userRequest, Long memberId) {
        return new ReservationRequest(
                userRequest.date(),
                userRequest.timeId(),
                userRequest.themeId(),
                memberId
        );
    }

    public Reservation toEntityByReserved(ReservationTime reservationTime, Theme theme, Member member, Status status) {
        return new Reservation(
                date,
                reservationTime,
                theme,
                member,
                status
        );
    }
}
