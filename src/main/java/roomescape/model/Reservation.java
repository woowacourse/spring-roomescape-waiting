package roomescape.model;

import static roomescape.model.ReservationStatus.ACCEPT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import jdk.jfr.Timestamp;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    @Timestamp
    private LocalDateTime createdAt;
    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Theme theme;
    @ManyToOne
    private Member member;

    protected Reservation() {
    }

    public Reservation(Long id,
                       LocalDate date,
                       ReservationStatus status,
                       LocalDateTime createdAt,
                       ReservationTime time,
                       Theme theme,
                       Member member) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.createdAt = createdAt;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Reservation(Long id,
                       LocalDate date,
                       ReservationStatus status,
                       ReservationTime time,
                       Theme theme,
                       Member member) {
        this(id, date, status, null, time, theme, member);
    }

    public Reservation(Long id, LocalDate date, ReservationTime time, Theme theme, Member member) {
        this(id, date, ACCEPT, time, theme, member);
    }

    public Reservation(LocalDate date, ReservationStatus status, ReservationTime time, Theme theme, Member member) {
        this(null, date, status, time, theme, member);
    }

    public Reservation(LocalDate date, ReservationTime time, Theme theme, Member member) {
        this(null, date, time, theme, member);
    }

    public long getId() {
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

    public Member getMember() {
        return member;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void confirmReservation() {
        this.status = ACCEPT;
    }

    public boolean isAcceptReservation() {
        return status == ACCEPT;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Reservation that = (Reservation) object;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getDate(), that.getDate())
                && getStatus() == that.getStatus() && Objects.equals(getTime(), that.getTime())
                && Objects.equals(getTheme(), that.getTheme()) && Objects.equals(getMember(),
                that.getMember());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDate(), getStatus(), getTime(), getTheme(), getMember());
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
