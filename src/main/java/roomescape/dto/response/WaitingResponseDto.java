package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Waiting;

public record WaitingResponseDto(
        Long id,
        String name,
        String theme,
        LocalDate date,
        LocalTime startAt
) {
    public static WaitingResponseDto from(final Waiting waiting) {
        return new WaitingResponseDto(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getReservationTime().getStartAt()
        );
    }

}

