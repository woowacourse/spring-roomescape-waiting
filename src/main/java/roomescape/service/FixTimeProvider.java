package roomescape.service;

import java.time.LocalDateTime;
import roomescape.model.time.TimeProvider;

public class FixTimeProvider implements TimeProvider {

    private final LocalDateTime fixedDateTime;

    public FixTimeProvider(final LocalDateTime fixedDateTime) {
        this.fixedDateTime = fixedDateTime;
    }

    @Override
    public LocalDateTime getCurrentDateTime() {
        return this.fixedDateTime;
    }
}
