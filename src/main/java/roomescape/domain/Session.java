package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private TimeSlot timeSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    protected Session() {
    }

    public Session(Long id, LocalDate date, TimeSlot timeSlot, Theme theme) {
        validate(date, timeSlot, theme);
        this.id = id;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
    }

    public static Session transientOf(LocalDate date, TimeSlot timeSlot, Theme theme) {
        return new Session(null, date, timeSlot, theme);
    }

    public Optional<Waiting> promoteCandidate(List<Waiting> waitings) {
        return waitings.stream().min(Comparator.naturalOrder());
    }

    public boolean isPast(LocalDateTime currentDateTime) {
        LocalDateTime sessionDateTime = LocalDateTime.of(this.date, this.timeSlot.getStartAt());
        return sessionDateTime.isBefore(currentDateTime);
    }

    private void validate(LocalDate date, TimeSlot timeSlot, Theme theme) {
        if (date == null || timeSlot == null || theme == null) {
            throw new IllegalArgumentException("필수 세션 정보가 누락되었습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public Theme getTheme() {
        return theme;
    }
}
