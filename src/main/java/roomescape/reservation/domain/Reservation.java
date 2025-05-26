package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.exception.DomainValidationException;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
@Table(
    name = "reservation",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"date", "time_id", "theme_id", "priority"}
    )
)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Embedded
    private Priority priority;

    protected Reservation() {
    }

    public Reservation(LocalDate date, ReservationTime time, Theme theme, Member member, Priority priority) {
        validate(date, time, theme, member);
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.priority = priority;
    }

    public Reservation(LocalDate date, ReservationTime time, Theme theme, Member member) {
        validate(date, time, theme, member);
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.priority = Priority.first();
    }

    public Reservation(Long id, LocalDate date, ReservationTime time, Theme theme, Member member, Integer priority) {
        validate(date, time, theme, member);
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.priority = new Priority(priority);
    }

    Reservation(LocalDate date, ReservationTime time, Theme theme, Member member, Integer priority) {
        validate(date, time, theme, member);
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.priority = new Priority(priority);
    }

    public static Reservation first(LocalDate date, ReservationTime time, Theme theme, Member member) {
        return new Reservation(date, time, theme, member, Priority.first());
    }

    public static Reservation makeWaiting(
        Reservation reservation,
        Member member
    ) {
        return new Reservation(
            reservation.date,
            reservation.time,
            reservation.theme,
            member,
            reservation.priority.getValue() + 1
        );
    }

    public static Reservation generateWithPrimaryKey(Reservation reservation, Long newPrimaryKey) {
        return new Reservation(
            newPrimaryKey,
            reservation.date,
            reservation.time,
            reservation.theme,
            reservation.member,
            reservation.priority.getValue()
        );
    }

    private void validate(LocalDate date, ReservationTime time, Theme theme, Member member) {
        if (date == null || time == null || theme == null || member == null) {
            throw new DomainValidationException("예약 정보가 비어있습니다.");
        }
    }

    public void approve() {
        this.priority = priority.approve();
    }

    public boolean isWaiting() {
        return !priority.isHighest();
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

    public Integer getPriority() {
        return priority.getValue();
    }

    public Member getMember() {
        return member;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Reservation that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(member, that.member)
            && Objects.equals(date, that.date) && Objects.equals(time, that.time)
            && Objects.equals(theme, that.theme) && Objects.equals(priority, that.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, date, time, theme, priority);
    }
}
