package roomescape.domain.reservation.domain;

import jakarta.persistence.*;
import roomescape.domain.member.domain.Member;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.time.domain.ReservationTime;

import java.time.LocalDate;

@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @ManyToOne
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    protected Reservation() {

    }

    public Reservation(Long id, LocalDate date, ReservationStatus status, ReservationTime time, Theme theme, Member member) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public boolean isStatusReservation() {
        return status.isReservation();
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationStatus getStatus() {
        return status;
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

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", date=" + date +
                ", status=" + status +
                ", time=" + time +
                ", theme=" + theme +
                ", member=" + member +
                '}';
    }
}
