package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Entity
public class Time {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "time_id")
    private Long id;

    private LocalTime startAt;

    protected Time() {
    }

    public Time(LocalTime startAt) {
        this(null, startAt);
    }

    public Time(Long id, LocalTime startAt) {
        if (startAt == null) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "예약 시간은 필수 입력값 입니다.");
        }
        this.id = id;
        this.startAt = startAt;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Time that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
