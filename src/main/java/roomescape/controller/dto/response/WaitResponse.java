package roomescape.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Wait;

public record WaitResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        ReservationStatus status,
        Long order,
        LocalDateTime createdAt
) implements ReservationWaitResponse {

    public static WaitResponse of(Wait wait, Long order) {
        return new WaitResponse(
                wait.getId(),
                MemberResponse.from(wait.getMember()),
                wait.getReservationDate(),
                ReservationTimeResponse.from(wait.getTime()),
                ThemeResponse.from(wait.getTheme()),
                ReservationStatus.WAITING,
                order,
                wait.getCreatedAt()
        );
    }
}
