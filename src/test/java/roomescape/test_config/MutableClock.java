package roomescape.test_config;


import java.time.*;

public class MutableClock extends Clock {
    private Clock delegate;

    public MutableClock(Clock delegate) {
        this.delegate = delegate;
    }

    public void setFixed(LocalDateTime now) {
        this.delegate = Clock.fixed(
                now.atZone(getZone()).toInstant(),
                getZone()
        );
    }

    public void setFixed(LocalDate now) {
        this.delegate = Clock.fixed(
                now.atStartOfDay(getZone()).toInstant(),
                getZone()
        );
    }

    @Override
    public ZoneId getZone() {
        return delegate.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(delegate.withZone(zone));
    }

    @Override
    public Instant instant() {
        return delegate.instant();
    }
}
