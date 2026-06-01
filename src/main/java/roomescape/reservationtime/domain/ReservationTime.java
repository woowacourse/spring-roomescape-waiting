package roomescape.reservationtime.domain;

import lombok.Getter;
import roomescape.global.exception.InvalidRequestException;

import java.time.LocalTime;
import java.util.Objects;

@Getter
public class ReservationTime {
    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime(Long id, LocalTime startAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validateStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new InvalidRequestException("예약 시간은 비어 있을 수 없습니다.");
        }
    }

    public ReservationTime withId(Long id) {
        validateId(id);

        if (this.id != null) {
            throw new InvalidRequestException("이미 id가 존재하는 예약 시간입니다.");
        }

        return new ReservationTime(id, startAt);
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new InvalidRequestException("예약 시간 id는 비어 있을 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationTime that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
