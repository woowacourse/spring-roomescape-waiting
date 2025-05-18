package roomescape.application;

import java.time.LocalDateTime;

public interface ClockProvider {

    LocalDateTime now();
}
