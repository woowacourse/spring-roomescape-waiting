package roomescape.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.global.ReservationStatus;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    protected Reservation() {
    }

    public Reservation(Long id,
                       Member member,
                       LocalDate date,
                       ReservationTime reservationTime,
                       Theme theme,
                       ReservationStatus status) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.status = status;
    }

    public Reservation(LocalDate date,
                       ReservationTime reservationTime,
                       Theme theme,
                       ReservationStatus status) {
        this(null, null, date, reservationTime, theme, status);
    }

    public Reservation(Member member,
                       LocalDate date,
                       ReservationTime reservationTime,
                       Theme theme,
                       ReservationStatus status) {
        this(null, member, date, reservationTime, theme, status);
    }

    public boolean isBefore(LocalDateTime compareDateTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, getStartAt());
        return reservationDateTime.isBefore(compareDateTime);
    }

    public void cancel() {
        if (member != null) {
            member.removeReservation(this);
        }
    }

    public void changeStatusWaitToReserve() {
        if (member != null) {
            member.waitToReserve(date, reservationTime, theme);
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String getName() {
        return member.getName();
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public LocalTime getStartAt() {
        return reservationTime.getStartAt();
    }

    public Theme getTheme() {
        return theme;
    }

    public String getThemeName() {
        return theme.getName();
    }

    public ReservationStatus getStatus() {
        return status;
    }

    protected void setMember(Member member) {
        this.member = member;
    }

    protected void setStatus(ReservationStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Reservation that)) {
            return false;
        }
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
