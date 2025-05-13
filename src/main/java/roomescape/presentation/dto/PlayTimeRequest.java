package roomescape.presentation.dto;

import java.time.LocalTime;
import roomescape.business.domain.PlayTime;

public record PlayTimeRequest(LocalTime startAt) {

    public PlayTime toDomain() {
        return new PlayTime(startAt);
    }
}
