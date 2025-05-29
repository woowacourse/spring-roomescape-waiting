package roomescape.dto.reservation;

import java.time.LocalDate;
import roomescape.domain.waiting.Waiting;

public record ReservationCreateRequest(LocalDate date, Long themeId, Long timeId, Long memberId) {
    public static ReservationCreateRequest from(Waiting waiting) {
        return new ReservationCreateRequest(waiting.getDate(), waiting.getTheme().getId(), waiting.getTime().getId(),
                waiting.getMember().getId());
    }
}
