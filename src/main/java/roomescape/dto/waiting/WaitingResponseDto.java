package roomescape.dto.waiting;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.waiting.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingResponseDto(long id,
                                 String name,
                                 @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                 String theme,
                                 @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public static WaitingResponseDto from(Waiting waiting) {
        return new WaitingResponseDto(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getDate(),
                waiting.getTheme().getName(),
                waiting.getTime().getStartAt());
    }
}
