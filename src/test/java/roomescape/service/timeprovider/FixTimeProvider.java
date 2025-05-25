package roomescape.service.timeprovider;

import java.time.LocalDateTime;

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
