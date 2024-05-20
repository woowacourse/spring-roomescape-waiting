package roomescape.domain.reservation;

import jakarta.persistence.*;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.Objects;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private LocalTime startAt;

    protected ReservationTime() {
    }

    public ReservationTime(final LocalTime startAt) {
        this.startAt = startAt;
    }

    public static ReservationTime from(final String startAt) {
        try {
            return new ReservationTime(LocalTime.parse(startAt));
        } catch (final DateTimeException exception) {
            throw new IllegalArgumentException(String.format("%s 는 유효하지 않은 값입니다.(EX: 10:00)", startAt));
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public String getStartAtAsString() {
        return startAt.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final ReservationTime that)) return false;
        return Objects.equals(startAt, that.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(startAt);
    }
}
