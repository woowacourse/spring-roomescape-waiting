package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.exception.InvalidInputException;
import roomescape.domain.exception.PastReservationException;

@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate date;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    protected Reservation() {
    }

    public Reservation(Long id, String name, LocalDate date, LocalDateTime createdAt, ReservationTime time, Theme theme, ReservationStatus status) {
        validateFields(name, date, time, theme);
        this.id = id;
        this.name = name;
        this.date = date;
        this.createdAt = createdAt;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public Reservation(Long id, String name, LocalDate date, LocalDateTime createdAt, ReservationTime time, Theme theme) {
        this(id, name, date, createdAt, time, theme, ReservationStatus.CONFIRMED);
    }

    public Reservation(String name, LocalDate date, LocalDateTime createdAt, ReservationTime time, Theme theme) {
        this(null, name, date, createdAt, time, theme, ReservationStatus.CONFIRMED);
        validateNotPast(date, time, createdAt);
    }

    public Reservation(String name, LocalDate date, LocalDateTime createdAt, ReservationTime time, Theme theme, ReservationStatus status) {
        this(null, name, date, createdAt, time, theme, status);
        validateNotPast(date, time, createdAt);
    }

    public Reservation withUpdated(LocalDate date, ReservationTime newTime, LocalDateTime now) {
        validateNotPast(date, newTime, now);
        return new Reservation(id, name, date, this.createdAt, newTime, theme, this.status);
    }

    public void validateCancellable(LocalDateTime now) {
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(now)) {
            throw new PastReservationException("이미 지난 예약은 취소할 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        return Objects.equals(name, that.name)
                && Objects.equals(date, that.date)
                && Objects.equals(time, that.time)
                && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        if (id != null) return Objects.hash(id);
        return Objects.hash(name, date, time, theme);
    }

    private void validateFields(String name, LocalDate date, ReservationTime time, Theme theme) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("예약자 이름은 비어있을 수 없습니다.");
        }
        if (date == null) {
            throw new InvalidInputException("예약 날짜는 필수입니다.");
        }
        if (time == null) {
            throw new InvalidInputException("예약 시간은 필수입니다.");
        }
        if (theme == null) {
            throw new InvalidInputException("테마는 필수입니다.");
        }
    }

    private static void validateNotPast(LocalDate date, ReservationTime time, LocalDateTime now) {
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(now)) {
            throw new PastReservationException("과거 날짜로는 예약할 수 없습니다.");
        }
    }
}
