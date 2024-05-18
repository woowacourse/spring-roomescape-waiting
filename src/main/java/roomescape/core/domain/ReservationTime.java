package roomescape.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Entity
public class ReservationTime {
    public static final String TIME_FORMAT = "HH:mm";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalTime startAt;

    public ReservationTime() {
    }

    public ReservationTime(final String startAt) {
        this(null, startAt);
    }

    public ReservationTime(final Long id, final String startAt) {
        this.id = id;
        this.startAt = parseStartAt(startAt);
    }

    private LocalTime parseStartAt(final String startAt) {
        try {
            return LocalTime.parse(startAt);
        } catch (final DateTimeParseException e) {
            throw new IllegalArgumentException("시간 형식이 잘못되었습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public String getStartAtString() {
        return startAt.format(DateTimeFormatter.ofPattern(TIME_FORMAT));
    }

    public boolean isPast() {
        return startAt.isBefore(LocalTime.now());
    }
}
