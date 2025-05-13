package roomescape.fake;

import roomescape.application.CurrentTimeService;

import java.time.LocalDateTime;

public class FixedCurrentTimeService implements CurrentTimeService {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.of(2025, 4, 20, 10, 0);
    }
}
