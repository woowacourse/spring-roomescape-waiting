package roomescape.domain.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.reservation.vo.ReservationSchedule;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;

@Entity
@Table(
    name = "reservation",
    indexes = {
        @Index(
            name = "uq_waiting_reservation",
            columnList = "active_waiting, name, date, time_id, theme_id",
            unique = true
        ),
        @Index(
            name = "uq_active_reservation",
            columnList = "active_date, active_time_id, active_theme_id",
            unique = true
        )
    }
)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "date", nullable = false, columnDefinition = "DATE")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        columnDefinition = "ENUM('ACTIVE', 'CANCELED', 'WAITING') DEFAULT 'ACTIVE'"
    )
    private ReservationStatus status;

    @NotNull
    @Version
    @Column(name = "version", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long version;

    @Column(name = "deleted_at", columnDefinition = "DATETIME DEFAULT NULL")
    private LocalDateTime deletedAt;

    @Column(
        name = "active_date",
        insertable = false,
        updatable = false,
        columnDefinition = "DATE GENERATED ALWAYS AS "
            + "(CASE WHEN status = 'ACTIVE' AND deleted_at IS NULL THEN date ELSE NULL END)"
    )
    private LocalDate activeDate;

    @Column(
        name = "active_time_id",
        insertable = false,
        updatable = false,
        columnDefinition = "BIGINT GENERATED ALWAYS AS "
            + "(CASE WHEN status = 'ACTIVE' AND deleted_at IS NULL THEN time_id ELSE NULL END)"
    )
    private Long activeTimeId;

    @Column(
        name = "active_theme_id",
        insertable = false,
        updatable = false,
        columnDefinition = "BIGINT GENERATED ALWAYS AS "
            + "(CASE WHEN status = 'ACTIVE' AND deleted_at IS NULL THEN theme_id ELSE NULL END)"
    )
    private Long activeThemeId;

    @Column(
        name = "active_waiting",
        insertable = false,
        updatable = false,
        columnDefinition = "BOOLEAN GENERATED ALWAYS AS "
            + "(CASE WHEN status = 'WAITING' AND deleted_at IS NULL THEN true ELSE NULL END)"
    )
    private Boolean activeWaiting;

    @ManyToOne
    @JoinColumn(name = "time_id", nullable = false)
    private Time time;

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    private Reservation(Long id, String name, LocalDate date, Time time, Theme theme, ReservationStatus status,
        Long version) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.version = version;
    }

    public Reservation() {

    }

    public static Reservation create(String name, LocalDate date, Time time, Theme theme) {
        return new Reservation(null, name, date, time, theme, ReservationStatus.ACTIVE, 0L);
    }

    public static Reservation reconstruct(
        Long id, String name, LocalDate date, Time time, Theme theme, ReservationStatus status, Long version) {
        return new Reservation(id, name, date, time, theme, status, version);
    }

    public static Reservation reconstruct(
        Long id, String name, LocalDate date, Time time, Theme theme, ReservationStatus status) {
        return reconstruct(id, name, date, time, theme, status, 0L);
    }

    public Reservation cancel() {
        this.status = ReservationStatus.CANCELED;
        return this;
    }

    public Reservation toWaiting() {
        this.status = ReservationStatus.WAITING;
        return this;
    }

    public Reservation toActive() {
        this.status = ReservationStatus.ACTIVE;
        return this;
    }

    public void update(LocalDate date, Time time, Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public ReservationEditableStatus getEditableStatus(LocalDateTime now) {
        if (status == ReservationStatus.CANCELED) {
            return ReservationEditableStatus.CANCELED;
        }

        if (isPast(now) && isWaiting()) {
            return ReservationEditableStatus.WAITING_LOCKED;
        }

        if (isPast(now)) {
            return ReservationEditableStatus.LOCKED;
        }

        if (status == ReservationStatus.WAITING) {
            return ReservationEditableStatus.WAITING;
        }

        if (time.isDeleted() || theme.isDeleted()) {
            return ReservationEditableStatus.EDIT_RECOMMENDED;
        }

        return ReservationEditableStatus.EDITABLE;
    }

    public ReservationSchedule getSchedule() {
        return new ReservationSchedule(date, theme.getId(), time.getId());
    }

    public boolean isReservedBy(String name) {
        return this.name.equals(name);
    }

    public boolean isActive() {
        return status == ReservationStatus.ACTIVE;
    }

    public boolean isWaiting() {
        return status == ReservationStatus.WAITING;
    }

    public boolean isScheduleChanged(Reservation reservation) {
        return !getSchedule().equals(reservation.getSchedule());
    }

    public boolean hasVersion(Long version) {
        return Objects.equals(this.version, version);
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
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

    public Time getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }
}
