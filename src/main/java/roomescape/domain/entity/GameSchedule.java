package roomescape.domain.entity;

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
import java.util.Objects;

@Entity
@Table(
        name = "game_schedule",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_schedule_date_time_theme",
                columnNames = {"date", "time_id", "theme_id"}
        )
)
public class GameSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    private GameSchedule(Long id, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.date = Objects.requireNonNull(date, "날짜가 필요합니다.");
        this.time = Objects.requireNonNull(time, "예약 시간이 필요합니다.");
        this.theme = Objects.requireNonNull(theme, "방탈출 테마가 필요합니다.");
    }

    protected GameSchedule() {
    }

    public static GameSchedule withId(Long id, LocalDate date, ReservationTime time, Theme theme) {
        if (id == null) {
            throw new IllegalArgumentException("id를 입력해주세요.");
        }

        return new GameSchedule(id, date, time, theme);
    }

    public static GameSchedule withoutId(LocalDate date, ReservationTime time, Theme theme) {
        return new GameSchedule(null, date, time, theme);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
