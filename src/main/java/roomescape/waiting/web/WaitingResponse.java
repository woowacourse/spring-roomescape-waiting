package roomescape.waiting.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import roomescape.dto.response.ThemeResponseDto;
import roomescape.dto.response.TimeResponseDto;
import roomescape.reservation.ReservationStatus;
import roomescape.waiting.Waiting;

public record WaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationStatus status,
        Long rank,
        @JsonProperty("theme") ThemeResponseDto themeResponseDto,
        @JsonProperty("time") TimeResponseDto timeResponseDto
) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getDate(),
                ReservationStatus.WAITING,
                waiting.getRank(),
                ThemeResponseDto.from(waiting.getTheme()),
                TimeResponseDto.from(waiting.getTime())
        );
    }
}
