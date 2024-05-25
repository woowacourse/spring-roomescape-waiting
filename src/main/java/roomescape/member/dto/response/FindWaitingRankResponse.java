package roomescape.member.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.Waiting;

public record FindWaitingRankResponse(Long waitingId,
                                      String theme,
                                      LocalDate date,
                                      LocalTime time,
                                      Long waitingNumber) {
    public static FindWaitingRankResponse from(Waiting waiting) {
        return new FindWaitingRankResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getReservationTime().getStartAt(),
                1L);
    }
}
