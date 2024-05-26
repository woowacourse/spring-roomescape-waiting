package roomescape.domain.reservation.domain.reservation;

import static roomescape.domain.reservation.domain.reservation.ReservationStatus.RESERVED;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.member.domain.Member;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.domain.Theme;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private ReservationDate date;
    @ManyToOne
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;
    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Reservation() {

    }

    public Reservation(Long id, LocalDate date, ReservationTime time, Theme theme, Member member,
                       ReservationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.member = member;
        this.date = new ReservationDate(date);
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.createdAt = createdAt.withNano(0);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date.getValue();
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

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Reservation changeStatusToReserved() {
        return new Reservation(id, date.getValue(), time, theme, member, RESERVED, createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id) && Objects.equals(date, that.date)
                && Objects.equals(time, that.time) && Objects.equals(theme, that.theme)
                && Objects.equals(member, that.member) && Objects.equals(createdAt,
                that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, time, theme, member, createdAt);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", date=" + date +
                ", time=" + time +
                ", theme=" + theme +
                ", member=" + member +
                ", status=" + status +
                ", createAt=" + createdAt +
                '}';
    }
}
