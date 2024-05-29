package roomescape.entity;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import roomescape.domain.ReservationStatus;

@Entity
public class CanceledReservation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long reservationId;
    @Column(nullable = false)
    private LocalDate date;
    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Theme theme;
    @ManyToOne
    private Member member;
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;


    protected CanceledReservation() {

    }

    public CanceledReservation(Reservation reservation) {
        this.reservationId = reservation.getId();
        this.date = reservation.getDate();
        this.time = reservation.getReservationTime();
        this.theme = reservation.getTheme();
        this.member = reservation.getMember();
        this.status = reservation.getStatus();
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
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

    public ReservationStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CanceledReservation that = (CanceledReservation) o;
        return Objects.equals(id, that.id) && Objects.equals(reservationId, that.reservationId) && Objects.equals(date, that.date) && Objects.equals(time, that.time) && Objects.equals(theme, that.theme) && Objects.equals(member, that.member) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reservationId, date, time, theme, member, status);
    }

    @Override
    public String toString() {
        return "CanceledReservation{" +
                "id=" + id +
                ", reservationId=" + reservationId +
                ", date=" + date +
                ", time=" + time +
                ", theme=" + theme +
                ", member=" + member +
                ", status=" + status +
                '}';
    }
}
