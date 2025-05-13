package roomescape.business.domain;

import java.time.LocalTime;

public class PlayTime {

    private final Long id;
    private final LocalTime startAt;

    public PlayTime(final Long id, final LocalTime startAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public PlayTime(final LocalTime startAt) {
        this(null, startAt);
    }

    public PlayTime(final Long id) {
        this.id = id;
        this.startAt = null;
    }

    private void validateStartAt(final LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("startAt이 null 입니다.");
        }
    }

    public boolean isSamePlayTime(final PlayTime playTime) {
        return id.equals(playTime.getId());
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
