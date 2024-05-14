package roomescape.dto.reservation;

import java.time.LocalDate;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Reservation.Reservation;
import roomescape.domain.ReservationTime.ReservationTime;
import roomescape.domain.Theme.Theme;
import roomescape.domain.member.Member;

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

    public Reservation toEntity(ReservationTime reservationTime, Theme theme, Member member) {
        return new Reservation(
                date,
                reservationTime,
                theme,
                member
        );
    }
}
