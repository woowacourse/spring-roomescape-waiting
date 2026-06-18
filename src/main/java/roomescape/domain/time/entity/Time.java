package roomescape.domain.time.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
    name = "reservation_time",
    indexes = @Index(
        name = "uq_active_reservation_time",
        columnList = "active_start_at",
        unique = true
    )
)
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "start_at", nullable = false, columnDefinition = "TIME")
    private LocalTime startAt;

    @Column(name = "deleted_at", columnDefinition = "DATETIME DEFAULT NULL")
    private LocalDateTime deletedAt;

    @Column(
        name = "active_start_at",
        insertable = false,
        updatable = false,
        columnDefinition = "TIME GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN start_at ELSE NULL END)"
    )
    private LocalTime activeStartAt;

    public Time(Long id, LocalTime startAt, LocalDateTime deletedAt) {
        this.id = id;
        this.startAt = startAt;
        this.deletedAt = deletedAt;
    }

    public Time() {

    }

    public static Time create(LocalTime startAt) {
        return new Time(null, startAt, null);
    }

    public static Time reconstruct(Long id, LocalTime startAt, LocalDateTime deletedAt) {
        return new Time(id, startAt, deletedAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
