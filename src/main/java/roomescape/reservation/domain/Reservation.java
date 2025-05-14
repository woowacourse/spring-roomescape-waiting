package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.theme.domain.Theme;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    
    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    @ManyToOne
    private Member member;

    @Enumerated(EnumType.STRING)
    private Status status;

    public Reservation() {

    }

    public Reservation(final Long id, final LocalDate date, final ReservationTime time, final Theme theme,
                       final Member member, final Status status) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
        this.status = status;
    }

    public Reservation(final LocalDate date, final ReservationTime time, final Theme theme, final Member member, final Status status) {
        this(null, date, time, theme, member, status);
    }

    public boolean hasConflictWith(final ReservationTime reservationTime, final Theme theme) {
        final LocalTime startAt = time.getStartAt();
        return reservationTime.hasConflict(theme.getDuration(), startAt);
    }

    public Long getId() {
        return id;
    }

    public MemberName getName() {
        return member.getName();
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

    public Member getMember() {
        return member;
    }

    public String getThemeName() {
        return theme.getName();
    }

    public LocalTime getStartAt() {
        return time.getStartAt();
    }

    public Status getStatus() {
        return status;
    }

    public String getStatusValue() {
        return status.getValue();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
