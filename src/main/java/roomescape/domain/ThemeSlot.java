package roomescape.domain;

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
import java.util.Objects;

@Entity
@Table(
        name = "theme_slot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"theme_id", "date", "time_id"})
)
public class ThemeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "time_id", nullable = false)
    private Time time;

    @Column(name = "is_reserved", nullable = false)
    private boolean isReserved;

    protected ThemeSlot() {
    }

    public ThemeSlot(Theme theme, LocalDate date, Time time, boolean isReserved) {
        this.id = null;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.isReserved = isReserved;
    }

    public ThemeSlot(Long id, Theme theme, LocalDate date, Time time, boolean isReserved) {
        this.id = id;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.isReserved = isReserved;
    }

    public static ThemeSlot of(Long id, ThemeSlot themeSlot) {
        return new ThemeSlot(id, themeSlot.getTheme(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.isReserved());
    }

    public Long getId() {
        return id;
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public boolean hasSameId(Long id) {
        return Objects.equals(this.id, id);
    }

    public void reserve() {
        this.isReserved = true;
    }

    public void release() {
        this.isReserved = false;
    }
}
