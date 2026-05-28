package roomescape.test_config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.common.time.TimeManager;

public class MutableTimeManager extends TimeManager {

    private LocalDateTime fixedNow;

    public void setFixed(LocalDateTime now) {
        this.fixedNow = now;
    }

    public void setFixed(LocalDate now) {
        this.fixedNow = now.atStartOfDay();
    }

    public void reset() {
        this.fixedNow = null;
    }

    @Override
    public LocalDate today() {
        return now().toLocalDate();
    }

    @Override
    public LocalDateTime now() {
        if (fixedNow != null) {
            return fixedNow;
        }
        return super.now();
    }
}
