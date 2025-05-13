package roomescape.presentation.dto;

import java.time.LocalTime;
import roomescape.business.domain.PlayTime;

public record PlayTimeResponse(Long id, LocalTime startAt) {

    public static PlayTimeResponse from(final PlayTime playTime) {
        return new PlayTimeResponse(playTime.getId(), playTime.getStartAt());
    }
}
