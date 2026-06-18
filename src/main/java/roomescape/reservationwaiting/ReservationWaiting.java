package roomescape.reservationwaiting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

@Entity
@Table(name = "reservation_waiting",
    uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {
                        "date",
                        "theme_id",
                        "time_id",
                        "name"
                }
        )
    }
)
public class ReservationWaiting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @ManyToOne
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @Column(nullable = false)
    private String name;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestAt;

    protected ReservationWaiting() {}

    public ReservationWaiting(Long id, LocalDate date, final Theme theme, final ReservationTime time, String name, LocalDateTime requestAt) {
        this.id = id;
        this.date = date;
        this.theme = theme;
        this.time = time;
        this.name = validateName(name);
        this.requestAt = requestAt;
    }

    public static ReservationWaiting createNew(LocalDate date, Theme theme, ReservationTime time, String name, LocalDateTime requestAt) {
        return new ReservationWaiting(null, date, theme, time, name, requestAt);
    }

    public ReservationWaiting withId(final Long id) {
        return new ReservationWaiting(id, this.date, this.theme, this.time, this.name, this.requestAt);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() { return date; }

    public Theme getTheme() { return theme; }

    public ReservationTime getTime() { return time; }

    public String getName() {
        return name;
    }

    public LocalDateTime getRequestAt() {
        return requestAt;
    }

    private String validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약자 이름은 필수입니다.");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() >= 10) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약자 이름은 10자 미만이어야 합니다.");
        }

        return trimmedName;
    }
}
