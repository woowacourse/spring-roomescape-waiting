package roomescape.slot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Getter
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_slot_date_time_theme",
                columnNames = {"date", "time_id", "theme_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Slot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    private Slot(Long id, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.date = Objects.requireNonNull(date, "date는 null일 수 없습니다.");
        this.time = Objects.requireNonNull(time, "time은 null일 수 없습니다.");
        this.theme = Objects.requireNonNull(theme, "theme은 null일 수 없습니다.");
    }

    public static Slot create(LocalDate date, ReservationTime time, Theme theme) {
        return new Slot(null, date, time, theme);
    }

    public static Slot of(Long id, LocalDate date, ReservationTime time, Theme theme) {
        return new Slot(id, date, time, theme);
    }

    public Long getTimeId() {
        return time.id();
    }

    public LocalTime getStartAt() {
        return time.startAt();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.startAt()).isBefore(now);
    }

    public void validateNotPast(LocalDateTime now) {
        if (isPast(now)) {
            throw new EscapeRoomException(ErrorCode.PAST_SLOT);
        }
    }

}
