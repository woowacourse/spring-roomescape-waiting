package roomescape.dto.waiting;

import java.time.LocalDate;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonFormat;

public record WaitingRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        Long timeId,
        Long themeId,
        Long memberId
) {

    public WaitingRequest {
        Objects.requireNonNull(date);
        Objects.requireNonNull(timeId);
        Objects.requireNonNull(themeId);
        Objects.requireNonNull(memberId);
    }

    public static WaitingRequest from(UserWaitingRequest userRequest, Long memberId) {
        return new WaitingRequest(
                userRequest.date(),
                userRequest.timeId(),
                userRequest.themeId(),
                memberId
        );
    }
}
