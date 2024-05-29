package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.exceptions.MissingRequiredFieldException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    protected ReservationTime() {
    }

    public ReservationTime(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    private void validate(LocalTime startAt) {
        if (startAt == null) {
            throw new MissingRequiredFieldException("시작 시간은 필수 값입니다.");
        }
    }

    public boolean isBeforeNow(LocalDate date) {
        Instant instantToCompare = LocalDateTime.of(date, startAt)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toInstant();
        return instantToCompare.isBefore(Instant.now());
    }

    public boolean isBelongTo(List<Long> timeIds) {
        return timeIds.contains(id);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public String getStartAt(DateTimeFormatter formatter) {
        return startAt.format(formatter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
