package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import roomescape.system.exception.error.ErrorType;
import roomescape.system.exception.model.ValidateException;

@Entity
public class ReservationTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startAt;

    public ReservationTime() {
    }

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime(final Long id, final LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;

        validateNull();
    }

    private void validateNull() {
        if (startAt == null) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("예약 시간(Time) 생성에 유효하지 않은 값(null)이 입력되었습니다. [values: %s]", this));
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public String toString() {
        return "ReservationTime{" +
               "id=" + id +
               ", startAt=" + startAt +
               '}';
    }
}
