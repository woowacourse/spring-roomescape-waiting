package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.model.Waiting;

public record MemberWaitingResponseDto(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        int order
) {
    public MemberWaitingResponseDto(Waiting waiting, int order) {
        this(
                waiting.getId(),
                waiting.getThemeName(),
                waiting.getReservationDate(),
                waiting.getReservationTime().getStartAt(),
                order
        );
    }
}
