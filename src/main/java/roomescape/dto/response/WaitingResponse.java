package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.entity.Waiting;

public record WaitingResponse(
    Long id,
    MemberResponse member,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
    TimeResponse time,
    ThemeResponse theme) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
            waiting.getId(),
            MemberResponse.from(waiting.getMember()),
            waiting.getDate(),
            TimeResponse.from(waiting.getTime()),
            ThemeResponse.from(waiting.getTheme()));
    }
}
