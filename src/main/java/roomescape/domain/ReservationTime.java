package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "start_at", nullable = false, unique = true)
    private LocalTime startAt;

    @OneToMany(mappedBy = "time")
    private Set<Reservation> reservations;

    public ReservationTime() {
    }

    public ReservationTime(String startAt) {
        this(null, startAt);
    }

    public ReservationTime(Long id, String startAt) {
        this.id = id;
        this.startAt = parseTime(startAt);
    }

    private LocalTime parseTime(String rawTime) {
        try {
            return LocalTime.parse(rawTime);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("잘못된 시간 형식을 입력하셨습니다.");
        }
    }

    public boolean isBeforeNow() {
        return startAt.isBefore(LocalTime.now());
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

}
