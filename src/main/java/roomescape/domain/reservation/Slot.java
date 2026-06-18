package roomescape.domain.reservation;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static roomescape.domain.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.DomainPreconditions.requireNonNull;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(name = "uq_slot",
        columnNames = {"date", "time_id", "theme_id"}
        ))
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationDate date;

    @ManyToOne(optional = false)
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne(optional = false)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    /**
     * 미션 1 이후 양방향 삭제 고려
     *
     * - 테스트를 위한 양방향
     */
    @OneToMany(mappedBy = "slot")
    private List<Reservation> reservations = new ArrayList<>();

    protected Slot() {
    }

    public Slot(Long id, ReservationDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.date = requireNonNull(date, INVALID_INPUT, "예약일은 비어있을 수 없습니다.");
        this.time = requireNonNull(time, INVALID_INPUT, "예약 시간은 비어있을 수 없습니다.");
        this.theme = requireNonNull(theme, INVALID_INPUT, "예약 테마는 비어있을 수 없습니다.");
    }

    public static Slot load(Long id, LocalDate date, ReservationTime time, Theme theme) {
        return new Slot(id, new ReservationDate(date), time, theme);
    }

    public static Slot create(ReservationDate date, ReservationTime time, Theme theme, LocalDateTime now) {
        Slot slot = new Slot(null, date, time, theme);
        slot.validateNotPast(now);
        return slot;
    }

    public void validateNotPast(LocalDateTime now) {
        if (isPast(now)) {
            throw new RoomEscapeException(DomainErrorCode.PAST_DATE, "지나간 날짜/시간에는 예약할 수 없습니다: " + date.getDate() + " " + time.getStartAt());
        }
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date.getDate(), time.getStartAt()).isBefore(now);
    }

    public Long getId() {
        return id;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public ReservationDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
