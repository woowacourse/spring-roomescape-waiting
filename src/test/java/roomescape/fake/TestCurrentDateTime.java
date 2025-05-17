package roomescape.fake;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.CurrentDateTime;

public class TestCurrentDateTime implements CurrentDateTime {
    private LocalDateTime now;

    public TestCurrentDateTime(LocalDateTime now) {
        this.now = now;
    }

    public void changeDateTime(LocalDateTime now) {
        this.now = now;
    }

    @Override
    public LocalDate getDate() {
        return now.toLocalDate();
    }

    @Override
    public LocalTime getTime() {
        return now.toLocalTime();
    }

    @Override
    public LocalDateTime getDateTime() {
        return now;
    }
}
