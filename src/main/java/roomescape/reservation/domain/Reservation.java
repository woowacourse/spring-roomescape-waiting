package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import roomescape.member.domain.Member;

@Entity
public class Reservation {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id", nullable = false))
    private ReservationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    protected Reservation() {}

    public Reservation(
            final Long id,
            final Member member,
            final ReservationStatus status,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        this.id = new ReservationId(id);
        this.member = member;
        this.status = status;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(
            final Member member,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        this(null, member, ReservationStatus.CONFIRMED, date, time, theme);
    }

    public Long getId() {
        return id.getValue();
    }

    public Member getMember() {
        return member;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public String statusDescription() {
        return status.getDescription();
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public LocalTime startTime() {
        return time.getStartAt();
    }

    public Theme getTheme() {
        return theme;
    }

    public String themeName() {
        return theme.getName();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
